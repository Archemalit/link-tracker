package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import java.time.LocalTime;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Profile("jpa")
public interface JpaChatRepository extends JpaRepository<Chat, Long>, ChatRepository {
    @Override
    @Modifying
    @Transactional
    @Query("UPDATE Chat c SET c.notificationTime = :notificationTime WHERE c.telegramChatId = :telegramChatId")
    int updateNotificationTimeByTelegramChatId(
            @Param("telegramChatId") Long telegramChatId, @Param("notificationTime") LocalTime notificationTime);
}
