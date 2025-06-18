package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.dto.request.AddNotificationTimeRequest;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import backend.academy.scrapper.service.ChatService;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class ChatServiceImpl implements ChatService {
    private final ChatRepository chatRepository;

    @Override
    public void registerChat(long telegramChatId) {
        // TODO: тут тоже можно переделать всё в один запрос с ON CONFLICT (telegram_chat_id) DO NOTHING (для jdbc
        //  update использовать)
        if (!chatRepository.existsChatByTelegramChatId(telegramChatId)) {
            Chat chat = Chat.builder().telegramChatId(telegramChatId).build();
            chatRepository.save(chat);
            log.info("Чат с id {} зарегистрирован для отслеживания ссылок", telegramChatId);
        } else {
            log.info("Чат с id {} уже зарегистрирован", telegramChatId);
        }
    }

    @Override
    public void deleteChat(long telegramChatId) {
        if (chatRepository.deleteByTelegramChatId(telegramChatId) == 0) {
            log.info("Чата с id {} не существует", telegramChatId);
            throw new ChatNotFoundException(telegramChatId);
        }
        log.info("Чат с id {} был удалён", telegramChatId);
    }

    @Override
    public void addNotificationType(long telegramChatId, AddNotificationTimeRequest addNotificationTimeRequest) {
        LocalTime notificationTime = addNotificationTimeRequest.notificationTime();
        int updated = chatRepository.updateNotificationTimeByTelegramChatId(telegramChatId, notificationTime);
        if (updated == 0) {
            log.info("Чата с id {} не существует", telegramChatId);
            throw new ChatNotFoundException(telegramChatId);
        }
        log.info(
                "Для чата с id {} был новый режим отправки обновлений: {}",
                telegramChatId,
                notificationTime == null ? "сразу же" : "в " + notificationTime);
    }
}
