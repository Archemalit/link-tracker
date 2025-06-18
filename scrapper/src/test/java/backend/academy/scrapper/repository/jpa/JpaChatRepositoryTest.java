package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.IntegrationEnvironment;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("jpa")
public class JpaChatRepositoryTest extends IntegrationEnvironment {
    @Autowired
    private ChatRepository jpaChatRepository;

    private Long telegramChatId;
    private Chat chat;

    @BeforeEach
    void setUp() {
        telegramChatId = 12345L;
        chat = Chat.builder().telegramChatId(telegramChatId).build();
        jpaChatRepository.save(chat);
    }

    @Test
    @DisplayName("Проверка существования чата по telegram_chat_id")
    void testExistsChatByTelegramChatId() {
        // WHEN & THEN
        assertThat(jpaChatRepository.existsChatByTelegramChatId(telegramChatId)).isTrue();
    }

    @Test
    @DisplayName("Поиск чата по telegram_chat_id")
    void testFindChatByTelegramChatId() {
        // WHEN
        Chat foundChat = jpaChatRepository.findFirstByTelegramChatId(telegramChatId);

        // THEN
        assertThat(foundChat).isNotNull();
        assertThat(foundChat.id()).isEqualTo(chat.id());
    }

    @Test
    @DisplayName("Удаление чата по telegram_chat_id")
    void testDeleteByTelegramChatId() {
        // WHEN
        jpaChatRepository.deleteByTelegramChatId(telegramChatId);

        // THEN
        assertThat(jpaChatRepository.existsChatByTelegramChatId(telegramChatId)).isFalse();
    }
}
