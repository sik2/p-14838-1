package com.ll.back.global.chatConfig;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem("""
                당신은 친절한 한국어 상담사입니다. 
                당신은 사용자의 질문에 대해 친절하고 상세하게 답변해야 합니다. 
                시작할때 날씨와 관련된 주제로 스몰토크를 해주세요.
                """)
                .build();
    }
}
