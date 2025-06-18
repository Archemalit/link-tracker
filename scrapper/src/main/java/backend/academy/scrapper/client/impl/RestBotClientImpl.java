package backend.academy.scrapper.client.impl;

import backend.academy.scrapper.client.RestBotClient;
import backend.academy.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.scrapper.exception.BotNotificationFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Log4j2
@RequiredArgsConstructor
public class RestBotClientImpl implements RestBotClient {
    private final RestClient restClient;
    private final String updates;

    @Override
    public void notifyUser(LinkUpdateRequest linkUpdate) {
        try {
            restClient
                    .post()
                    .uri(updates)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(linkUpdate)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException ex) {
            // TODO: что-то сделать с тем, что запрос всё равно выполняется, но долго
            log.error("Ошибка при отправке уведомления боту", ex);
            throw new BotNotificationFailedException("Не удалось уведомить пользователя: " + ex.getMessage());
        }
    }
}
