package backend.academy.scrapper.service.impl.sender;

import backend.academy.scrapper.config.KafkaUpdateTopicPropertiesConfig;
import backend.academy.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import backend.academy.scrapper.service.BotUpdateSender;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@AllArgsConstructor
public class QueueBotUpdateSender implements BotUpdateSender {
    private final KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;
    private final KafkaUpdateTopicPropertiesConfig kafkaTopicProperties;
    private final ChatRepository chatRepository;
    private final DigestStorage digestStorage;
    private final CircuitBreaker circuitBreaker;

    @Override
    public void sendUpdate(LinkUpdateRequest linkUpdate, boolean immediate) {
        // TODO: добавить тут логирование
        long telegramChatId = linkUpdate.telegramChatId();
        Chat chat = chatRepository.findFirstByTelegramChatId(telegramChatId);
        if (Objects.isNull(chat)) {
            log.info("Чата с id {} не существует", telegramChatId);
            throw new ChatNotFoundException(telegramChatId);
        }

        if (Objects.isNull(chat.notificationTime()) || immediate) {
            kafkaTemplate.send(kafkaTopicProperties.name(), linkUpdate);
        } else {
            digestStorage.appendToDigest(telegramChatId, linkUpdate);
        }
    }
}
