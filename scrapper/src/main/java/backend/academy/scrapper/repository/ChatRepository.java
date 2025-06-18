package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.jpa.entity.Chat;
import java.time.LocalTime;
import org.springframework.transaction.annotation.Transactional;

public interface ChatRepository {
    Chat save(Chat chat);

    @Transactional
    int deleteByTelegramChatId(Long telegramChatId);

    boolean existsChatByTelegramChatId(Long telegramChatId);

    Chat findFirstByTelegramChatId(Long telegramChatId);

    int updateNotificationTimeByTelegramChatId(Long telegramChatId, LocalTime notificationTime);
}
