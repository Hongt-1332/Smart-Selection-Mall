package edu.fafu.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import edu.fafu.database.entity.Ai;
import edu.fafu.database.mapper.businessmapper.merchantmapper.AiMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.List;

@Configuration
@EnableTransactionManagement
public class AppBeanConfig {

    private static final Logger log = LoggerFactory.getLogger(AppBeanConfig.class);

    private final SystemConfig systemConfig;

    public AppBeanConfig(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public WxMaService wxMaService() {
        return new WxMaServiceImpl();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    @Bean
    public ChatModel chatModel() {
        return OllamaChatModel.builder()
                .baseUrl(systemConfig.getAiBaseUrl())
                .modelName(systemConfig.getAiDefaultModel())
                .build();
    }

    @Bean
    public StreamingChatModel streamingChatModel() {
        return OllamaStreamingChatModel.builder()
                .baseUrl(systemConfig.getAiBaseUrl())
                .modelName(systemConfig.getAiDefaultModel())
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OllamaEmbeddingModel.builder()
                .baseUrl(systemConfig.getAiBaseUrl())
                .modelName(systemConfig.getAiEmbeddingModel())
                .build();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(EmbeddingModel embeddingModel, AiMapper aiMapper) {
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        try {
            Ai query = new Ai();
            query.setDelete(false);
            List<TextSegment> segments = aiMapper.selectList(query).stream()
                    .map(p -> TextSegment.from(p.toTextSegment())).toList();

            if (segments.isEmpty()) {
                log.warn("ai表无数据，跳过向量嵌入");
                return store;
            }

            int batchSize = systemConfig.getAiEmbeddingBatchSize();
            log.info("开始嵌入 {} 个商品（每批 {} 个）...", segments.size(), batchSize);

            for (int i = 0; i < segments.size(); i += batchSize) {
                int end = Math.min(i + batchSize, segments.size());
                List<TextSegment> batch = segments.subList(i, end);
                try {
                    store.addAll(embeddingModel.embedAll(batch).content(), batch);
                    log.info("已嵌入 {}/{}", end, segments.size());
                } catch (Exception e) {
                    log.warn("第 {} 批嵌入失败: {}", (i / batchSize + 1), e.getMessage());
                }
            }

            log.info("向量库初始化完成，共 {} 个商品", segments.size());
        } catch (Exception e) {
            log.warn("向量库初始化失败，Ollama 可能未启动：{}", e.getMessage());
        }
        return store;
    }

    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore<TextSegment> embeddingStore,
                                              EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(systemConfig.getAiRagMaxResults())
                .minScore(systemConfig.getAiRagMinScore())
                .build();
    }
}