package com.ll.back.chatAi;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
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
    public Map<String, String> chat(@RequestParam String message) {
        String response = chatClient.prompt()
                .user(message)
                .call()
                .content();

        return Map.of("응답 : ", response);
    }
}
