package com.ll.back.global.chatConfig;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.MysqlChatMemoryRepositoryDialect;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@Configuration
public class ChatConfig {

    /**
     * 대화 이력을 MySQL에 읽고 쓰는 저장소
     * Dialect로 DB 종류 지정 (PostgreSQL이면 PostgresChatMemoryRepositoryDialect)
     */
    @Bean
    public JdbcChatMemoryRepository jdbcChatMemoryRepository(JdbcTemplate jdbcTemplate) {
        return JdbcChatMemoryRepository.builder()
                .jdbcTemplate(jdbcTemplate)
                .dialect(new MysqlChatMemoryRepositoryDialect())
                .build();
    }

    /**
     * 대화 메모리 - 이전 대화 내용을 기억
     * MessageWindowChatMemory: 최근 N개 메시지만 유지 (슬라이딩 윈도우)
     * chatMemoryRepository를 지정하면 DB에 저장 → 서버 재시작해도 유지
     */
    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
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

                // 여기 명시한 필드만 yml 설정을 덮어씀 (병합 방식)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("meta-llama/llama-4-scout-17b-16e-instruct")  // Groq 모델
                        .temperature(0.7)           // 0.0=일관적/결정적, 2.0=창의적/무작위
                        .maxTokens(1000)            // 응답 최대 길이 (비용 방어선)
                        .topP(1.0)                  // 누적 확률 상위 P만 후보로 삼음. temperature와 같이 안 건드는 게 관례
                        .frequencyPenalty(0.0)      // -2.0~2.0. 양수면 같은 단어 반복 억제
                        .presencePenalty(0.0)       // -2.0~2.0. 양수면 이미 나온 주제 회피 → 새 화제로 유도
                        .n(1)                       // 한 번에 생성할 응답 개수 (2.0에서 N() → n() 으로 변경)
                        .stopSequences(List.of())   // 이 문자열이 나오면 생성 중단
                        .user("user-id"))           // OpenAI 남용 탐지용 최종 사용자 식별자

                .build();
    }
}