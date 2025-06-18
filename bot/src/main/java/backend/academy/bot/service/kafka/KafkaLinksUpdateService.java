package backend.academy.bot.service.kafka;

import backend.academy.bot.dto.LinkUpdate;
import backend.academy.bot.service.link.LinkUpdateService;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaLinksUpdateService {
    private final LinkUpdateService linkUpdateService;

    @KafkaListener(topics = "${kafka.topic.name}", containerFactory = "containerFactory")
    public void consume(@Payload LinkUpdate update, Acknowledgment acknowledgment) {
        linkUpdateService.updateLink(update);
        acknowledgment.acknowledge();
    }
}
