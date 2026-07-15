package com.ll.back.domain.chatAi.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/v1/chat")
@RestController
@RequiredArgsConstructor
public class ApiV1ChatController {
    private final ChatClient chatClient;

    @GetMapping("/ai")
    public Map<String, String> chat(@RequestParam String message,
    @RequestParam(defaultValue = "default") String conversationId
    ) {
        String response = chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        return Map.of("응답 : ", response);
    }
}
