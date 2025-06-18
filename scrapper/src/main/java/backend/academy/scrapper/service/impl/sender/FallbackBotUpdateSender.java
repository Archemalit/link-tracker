package backend.academy.scrapper.service.impl.sender;

import backend.academy.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.scrapper.service.BotUpdateSender;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Log4j2
@Primary
@Component
@RequiredArgsConstructor
public class FallbackBotUpdateSender implements BotUpdateSender {
    private final QueueBotUpdateSender kafkaSender;
    private final HttpBotUpdateSender httpSender;
    private final CircuitBreaker circuitBreaker;

    @Value("${app.message-transport}")
    private String primaryTransport;

    @Override
    public void sendUpdate(LinkUpdateRequest linkUpdate, boolean immediate) {
        Runnable sender;
        if (primaryTransport.equals("kafka")) {
            sender = circuitBreaker.decorateRunnable(() -> kafkaSender.sendUpdate(linkUpdate, immediate));
        } else {
            sender = circuitBreaker.decorateRunnable(() -> httpSender.sendUpdate(linkUpdate, immediate));
        }
        try {
            sender.run();
        } catch (Exception ex) {
            if (!primaryTransport.equals("kafka")) {
                sender = circuitBreaker.decorateRunnable(() -> kafkaSender.sendUpdate(linkUpdate, immediate));
            } else {
                sender = circuitBreaker.decorateRunnable(() -> httpSender.sendUpdate(linkUpdate, immediate));
            }
            try {
                sender.run();
            } catch (Exception e) {
                log.warn("Ни один транспорт не смог отправить обновление: {}", e.getMessage());
                // TODO: значит тут вообще никак не получилось отправить
            }
        }
    }
}
