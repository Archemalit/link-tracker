package backend.academy.scrapper.service.impl.sender;

import backend.academy.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import backend.academy.scrapper.service.BotUpdateSender;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DigestScheduler {
    private final DigestStorage digestStorage;
    private final BotUpdateSender botUpdateSender;
    private final ChatRepository chatRepository;

    @Scheduled(fixedRate = 60000)
    public void sendScheduledDigests() {
        Set<Long> telegramChatIds = digestStorage.getAllChatIdsWithDigests();
        LocalTime now = LocalTime.now(ZoneId.systemDefault()).truncatedTo(ChronoUnit.MINUTES);

        for (Long telegramChatId : telegramChatIds) {
            Chat chat = chatRepository.findFirstByTelegramChatId(telegramChatId);
            if (Objects.nonNull(chat)
                    && (Objects.isNull(chat.notificationTime())
                            || chat.notificationTime().equals(now))) {
                List<LinkUpdateRequest> updates = digestStorage.getDigestAndClear(telegramChatId);
                for (LinkUpdateRequest linkUpdateRequest : updates) {
                    botUpdateSender.sendUpdate(linkUpdateRequest, true);
                }
            }
        }
    }
}
