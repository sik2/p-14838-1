package com.ll.back.global.chatConfig;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    /**
     * 대화 메모리 - 이전 대화 내용을 기억
     * MessageWindowChatMemory: 최근 N개 메시지만 유지 (슬라이딩 윈도우)
     * 기본은 인메모리 저장이라 서버 재시작하면 날아감.
     * 운영에서는 JdbcChatMemoryRepository 등으로 교체
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)  // 최근 20개 메시지 유지, 초과 시 오래된 것부터 제거
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                // 매 요청마다 자동으로 앞에 붙는 시스템 프롬프트
                .defaultSystem("""
                        당신은 친절한 한국어 어시스턴트입니다.
                        답변은 항상 한국어로, 간결하게 작성하세요.
                        시작할때 날씨와 관련된 주제로 스몰토크를 해주세요.
                        """)

                // Advisor = 요청/응답을 가로채는 인터셉터 체인 (스프링 AOP와 비슷한 개념)
                .defaultAdvisors(
                        // 대화 이력을 프롬프트에 자동 주입 → 멀티턴 대화 가능
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),

                        // 요청/응답을 DEBUG 레벨로 로깅
                        // application.yml에 아래 설정 필요:
                        // logging.level.org.springframework.ai.chat.client.advisor: DEBUG
                        new SimpleLoggerAdvisor()
                )
                .build();
    }
}