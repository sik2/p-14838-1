package com.ll.back.global.chatConfig;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class ChatClientTest {
    @Autowired
    private ChatModel chatModel; // 단순 호출용

    @Autowired
    private ChatClient chatClient; // 채팅방 별로 기억력이 있는 호출용

    @Test
    @DisplayName("단순 응답")
    void t1() {
        // Given
        String msg = "1+1은 얼마야?";

        // When
        String response = chatModel.call(msg);

        assertThat(response)
                .contains("2");
    }

    @Test
    @DisplayName("시스템 메세지 추가")
    void t2() {
        // Given
        String msg = "너의 이름과 나이와 취미를 소개해줘";

        // When
        String response = chatModel.call(
                new SystemMessage("""
                        이제부터 너는 한국 여자 24세 김민지야.
                        너는 K팝을 좋아해
                        """),
                new UserMessage(msg)
        );

        assertThat(response)
                .contains("김민지")
                .contains("K팝")
                .contains("24");
    }

    @Test
    @DisplayName("chatClient를 이용해서 지난 대화를 기억")
    void t3() {
        // Given
        String chatRoomCode = "test-chat-room";
        String msg1 = "너는 29세 김민수야. 너는 축구를 좋아해";
        String msg2 = "너의 이름과 나이와 취미를 소개해줘";

        String response1 = chatClient
                .prompt()
                .advisors( // 채팅방 코드 설정, 필수
                        advisor -> advisor
                                .param(ChatMemory.CONVERSATION_ID, chatRoomCode)
                )
                .user(msg1)
                .call()
                .content();

        // When
        String response2 = chatClient
                .prompt()
                .advisors(
                        advisor -> advisor
                                .param(ChatMemory.CONVERSATION_ID, chatRoomCode)
                )
                .user(msg2)
                .call()
                .content();

        assertThat(response2)
                .contains("김민수")
                .contains("축구")
                .contains("29");
    }

    @Test
    @DisplayName("chatClient를 이용해서 지난 대화를 기억 + 시스템 메세지 추가")
    void t4() {
        // Given
        String chatRoomCode = "test-chat-room";
        String systemMsg = """
                너는 우리 쇼핑몰 쇼피파이의 AI 챗봇 `쇼피`야.
                우리 쇼핑몰에서는 당일배송(비쌈)과 익일배송을 제공해
                반품은 15일안에 가능해
                """;
        String msg1 = "나는 돈이 비싸도 빠른 배송이 가능해.";
        String msg2 = "내가 무슨 배송을 해야 빨리 나에게 물건이 오니? 그리고 반품은 몇일안에 해야 해?";

        String response1 = chatClient
                .prompt()
                .system(systemMsg)
                .advisors( // 채팅방 코드 설정, 필수
                        advisor -> advisor
                                .param(ChatMemory.CONVERSATION_ID, chatRoomCode)
                )
                .user(msg1)
                .call()
                .content();

        // When
        String response2 = chatClient
                .prompt()
                .system(systemMsg)
                .advisors(
                        advisor -> advisor
                                .param(ChatMemory.CONVERSATION_ID, chatRoomCode)
                )
                .user(msg2)
                .call()
                .content();

        assertThat(response2)
                .contains("당일")
                .contains("15");
    }
}

