package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.mapper.ChatMapper;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Profile("jdbc")
public class JdbcChatRepository implements ChatRepository {
    private static final String EXIST_CHAT_BY_TELEGRAM_CHAT_ID =
            "SELECT EXISTS (SELECT 1 FROM chat WHERE telegram_chat_id = ?)";
    private static final String SELECT_CHAT_BY_TELEGRAM_CHAT_ID =
            "SELECT * FROM chat WHERE telegram_chat_id = ? LIMIT 1";
    private static final String SAVE_CHAT = "INSERT INTO chat (telegram_chat_id) VALUES (?) RETURNING id";
    private static final String DELETE_CHAT_BY_TELEGRAM_CHAT_ID = "DELETE FROM chat WHERE telegram_chat_id = ?";
    private static final String UPDATE_NOTIFICATION_TIME_BY_TELEGRAM_CHAT_ID =
            "UPDATE chat SET notification_time = ? WHERE telegram_chat_id = ?";

    private final JdbcTemplate jdbcTemplate;
    private final ChatMapper chatMapper;

    @Override
    public Chat save(Chat chat) {
        Long chatId = jdbcTemplate.queryForObject(SAVE_CHAT, Long.class, chat.telegramChatId());
        chat.id(chatId);
        return chat;
    }

    @Override
    public int deleteByTelegramChatId(Long telegramChatId) {
        return jdbcTemplate.update(DELETE_CHAT_BY_TELEGRAM_CHAT_ID, telegramChatId);
    }

    @Override
    public boolean existsChatByTelegramChatId(Long telegramChatId) {
        Boolean result = jdbcTemplate.queryForObject(EXIST_CHAT_BY_TELEGRAM_CHAT_ID, Boolean.class, telegramChatId);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public Chat findFirstByTelegramChatId(Long telegramChatId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_CHAT_BY_TELEGRAM_CHAT_ID, chatMapper, telegramChatId);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public int updateNotificationTimeByTelegramChatId(Long telegramChatId, LocalTime notificationTime) {
        return jdbcTemplate.update(UPDATE_NOTIFICATION_TIME_BY_TELEGRAM_CHAT_ID, notificationTime, telegramChatId);
    }
}
