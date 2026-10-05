package edu.fafu.service.service.aiservice;

import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.response.ai.ChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface LangChainService {

    Result<ChatResponse> chat(String message, Integer userId);

    SseEmitter chatStream(String message, Integer userId);

    Result<String> chatHistory(Integer userId);

    Result<String> chatClear(Integer userId);
}