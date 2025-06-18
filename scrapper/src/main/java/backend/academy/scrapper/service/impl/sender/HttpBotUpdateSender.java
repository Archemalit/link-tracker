package backend.academy.scrapper.service.impl.sender;

import backend.academy.scrapper.client.RestBotClient;
import backend.academy.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.scrapper.exception.BotNotificationFailedException;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import backend.academy.scrapper.service.BotUpdateSender;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@AllArgsConstructor
public class HttpBotUpdateSender implements BotUpdateSender {
    private final RestBotClient restBotClient;
    private final ChatRepository chatRepository;
    private final DigestStorage digestStorage;

    @Override
    public void sendUpdate(LinkUpdateRequest linkUpdate, boolean immediate) {
        long telegramChatId = linkUpdate.telegramChatId();
        Chat chat = chatRepository.findFirstByTelegramChatId(telegramChatId);
        if (Objects.isNull(chat)) {
            log.info("Чата с id {} не существует", telegramChatId);
            throw new ChatNotFoundException(telegramChatId);
        }

        if (Objects.isNull(chat.notificationTime()) || immediate) {
            try {
                restBotClient.notifyUser(linkUpdate);
            } catch (BotNotificationFailedException ex) {
                log.info("Не удалось оповестить об обновлении по HTTP");
            }
        } else {
            digestStorage.appendToDigest(telegramChatId, linkUpdate);
        }
    }
}
