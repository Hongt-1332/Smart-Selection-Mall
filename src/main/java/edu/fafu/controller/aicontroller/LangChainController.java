package edu.fafu.controller.aicontroller;

import cn.dev33.satoken.stp.StpUtil;
import edu.fafu.database.entity.Result;
import edu.fafu.database.dto.response.ai.ChatResponse;
import edu.fafu.service.service.aiservice.LangChainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ai")
public class LangChainController {

    @Autowired
    private LangChainService langChainService;

    @PostMapping(value = "/chat", produces = "application/json;charset=UTF-8")
    public Result<ChatResponse> chat(@RequestBody String message) {
        return langChainService.chat(message, StpUtil.getLoginIdAsInt());
    }

    @PostMapping(value = "/chatStream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chatStream(@RequestBody String message) {
        return langChainService.chatStream(message, StpUtil.getLoginIdAsInt());
    }

    @GetMapping("/chatHistory")
    public Result<String> chatHistory() {
        return langChainService.chatHistory(StpUtil.getLoginIdAsInt());
    }

    @DeleteMapping("/chatClear")
    public Result<String> chatClear() {
        return langChainService.chatClear(StpUtil.getLoginIdAsInt());
    }
}