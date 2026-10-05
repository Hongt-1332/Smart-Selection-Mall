package edu.fafu.service.impl.aiimpl;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import edu.fafu.config.SystemConfig;
import edu.fafu.config.RedisChatMemoryStore;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.response.ai.ChatResponse;
import edu.fafu.database.dto.response.user.UserGoods;
import edu.fafu.database.mapper.businessmapper.merchantmapper.AiMapper;
import edu.fafu.database.mapper.pagemapper.UserPageMapper;
import edu.fafu.exception.BusinessExceptionInterface;
import edu.fafu.service.service.aiservice.LangChainService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class LangChainServiceImpl implements LangChainService, BusinessExceptionInterface {

    private static final Logger log = LoggerFactory.getLogger(LangChainServiceImpl.class);

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final ContentRetriever contentRetriever;
    private final AiMapper aiMapper;

    @Autowired
    private SystemConfig systemConfig;
    @Autowired
    private RedisChatMemoryStore memoryStore;
    @Autowired
    private UserPageMapper userPageMapper;
    private CustomerServiceAgent agent;

    public LangChainServiceImpl(ChatModel chatModel, StreamingChatModel streamingChatModel,
                                ContentRetriever contentRetriever, AiMapper aiMapper) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.contentRetriever = contentRetriever;
        this.aiMapper = aiMapper;
    }

    @PostConstruct
    void init() {
        agent = AiServices.builder(CustomerServiceAgent.class)
                .chatModel(chatModel)
                .contentRetriever(contentRetriever)
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.builder()
                        .id(memoryId)
                        .maxMessages(systemConfig.getAiChatMaxMessages())
                        .chatMemoryStore(memoryStore)
                        .build())
                .build();
    }

    @Override
    public Result<ChatResponse> chat(String message, Integer userId) {
        ensureNotNull(userId, "用户未登录");
        ensureNotBlank(message, "消息不能为空");

        String aiMessage = agent.chat(userId, message);
        List<Integer> goodsIds = searchGoodsIds(message);
        List<UserGoods> products = goodsIds.isEmpty()
                ? List.of()
                : userPageMapper.selectGoodsByIds(goodsIds, userId);
        return Result.success(new ChatResponse(aiMessage, products));
    }

    @Override
    public SseEmitter chatStream(String message, Integer userId) {
        ensureNotNull(userId, "用户未登录");
        ensureNotBlank(message, "消息不能为空");

        SseEmitter emitter = new SseEmitter(120_000L);

        List<Integer> goodsIds = searchGoodsIds(message);
        List<UserGoods> products = goodsIds.isEmpty()
                ? List.of()
                : userPageMapper.selectGoodsByIds(goodsIds, userId);

        if (!products.isEmpty()) {
            try {
                emitter.send(SseEmitter.event().name("products").data(products));
            } catch (Exception e) {
                log.warn("发送商品数据失败: {}", e.getMessage());
            }
        }

        List<ChatMessage> history = memoryStore.getMessages(userId);
        List<ChatMessage> messages = new java.util.ArrayList<>(history);
        messages.add(UserMessage.from(message));

        StringBuilder fullResponse = new StringBuilder();

        streamingChatModel.chat(messages, new StreamingChatResponseHandler() {
            @Override
            public void onPartialResponse(String partialResponse) {
                try {
                    fullResponse.append(partialResponse);
                    emitter.send(SseEmitter.event().name("message").data(partialResponse));
                } catch (Exception e) {
                    log.warn("SSE发送失败: {}", e.getMessage());
                }
            }

            @Override
            public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse response) {
                try {
                    List<ChatMessage> updated = new java.util.ArrayList<>(history);
                    updated.add(UserMessage.from(message));
                    updated.add(AiMessage.from(fullResponse.toString()));
                    memoryStore.updateMessages(userId, updated);
                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    emitter.complete();
                } catch (Exception e) {
                    log.warn("SSE完成失败: {}", e.getMessage());
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onError(Throwable error) {
                log.warn("流式响应错误: {}", error.getMessage());
                emitter.completeWithError(error);
            }
        });

        return emitter;
    }

    @Override
    public Result<String> chatHistory(Integer userId) {
        ensureNotNull(userId, "用户未登录");
        List<ChatMessage> messages = memoryStore.getMessages(userId);
        return Result.success(ChatMessageSerializer.messagesToJson(messages));
    }

    @Override
    public Result<String> chatClear(Integer userId) {
        ensureNotNull(userId, "用户未登录");
        memoryStore.deleteMessages(userId);
        return Result.success("会话已清空");
    }

    private static final Pattern GOODS_ID_PATTERN = Pattern.compile("商品编号\\s+(\\d+)");

    private List<Integer> searchGoodsIds(String message) {
        return contentRetriever.retrieve(new Query(message)).stream()
                .map(content -> {
                    TextSegment seg = content.textSegment();
                    var m = GOODS_ID_PATTERN.matcher(seg.text());
                    return m.find() ? Integer.parseInt(m.group(1)) : null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .limit(5)
                .toList();
    }

    interface CustomerServiceAgent {
        @SystemMessage("""
                你是「智选商城」的智能客服助手，名叫「小智」。
                你的职责是根据提供的商品信息，热情、专业地回答顾客的问题。
                规则：
                1. 只能基于提供的商品信息回答，不要编造商品信息
                2. 如果询问的商品不在知识库中，明确说明没有该商品
                3. 回答要热情友好，可以适当推荐相关商品
                4. 回答时输出商品的编号和价格
                5. 使用中文回答
                """)
        String chat(@MemoryId int memoryId, @dev.langchain4j.service.UserMessage String message);
    }
}