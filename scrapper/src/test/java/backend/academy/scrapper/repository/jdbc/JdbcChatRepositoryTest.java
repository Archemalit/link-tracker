package backend.academy.scrapper.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.IntegrationEnvironment;
import backend.academy.scrapper.mapper.ChatMapper;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@Import({JdbcChatRepository.class, ChatMapper.class})
@ActiveProfiles("jdbc")
public class JdbcChatRepositoryTest extends IntegrationEnvironment {

    @Autowired
    private ChatRepository jdbcChatRepository;

    private Chat chat;

    @BeforeEach
    void setUp() {
        chat = Chat.builder().telegramChatId(12345L).build();
        chat = jdbcChatRepository.save(chat);
    }

    @Test
    @DisplayName("Проверка существования чата по telegram_chat_id")
    void testExistsChatByTelegramChatId() {
        // WHEN
        boolean exists = jdbcChatRepository.existsChatByTelegramChatId(chat.telegramChatId());

        // THEN
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Поиск чата по telegram_chat_id")
    void testFindChatByTelegramChatId() {
        // WHEN
        Chat foundChat = jdbcChatRepository.findFirstByTelegramChatId(chat.telegramChatId());

        // THEN
        assertThat(foundChat).isNotNull();
        assertThat(foundChat.telegramChatId()).isEqualTo(chat.telegramChatId());
    }

    @Test
    @DisplayName("Удаление чата по telegram_chat_id")
    void testDeleteChatByTelegramChatId() {
        // WHEN
        jdbcChatRepository.deleteByTelegramChatId(chat.telegramChatId());

        // THEN
        assertThat(jdbcChatRepository.existsChatByTelegramChatId(chat.telegramChatId()))
                .isFalse();
    }
}
