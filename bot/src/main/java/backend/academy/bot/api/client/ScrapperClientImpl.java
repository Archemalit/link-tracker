package backend.academy.bot.api.client;

import backend.academy.bot.api.dto.AddLinkRequest;
import backend.academy.bot.api.dto.AddNotificationTimeRequest;
import backend.academy.bot.api.dto.ApiErrorResponse;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.api.dto.LinkResponse;
import backend.academy.bot.api.dto.ListLinksResponse;
import backend.academy.bot.api.dto.RemoveLinkRequest;
import backend.academy.bot.exception.ScrapperApiException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ScrapperClientImpl implements ScrapperClient {
    private final WebClient webClient;
    private final String chatUrl;
    private final String linkUrl;
    private final CircuitBreaker circuitBreaker;
    private static final ApiErrorResponse RETRY =
            new ApiErrorResponse("Сервер временно недоступен, повторите запрос позже", "408", "", "", List.of());

    @Override
    public ClientResponse<Void> registerChat(long telegramChatId) {
        return performVoidRequest(HttpMethod.POST, chatUrl + "/" + telegramChatId, null);
    }

    @Override
    public ClientResponse<Void> addNotificationType(long telegramChatId, AddNotificationTimeRequest notificationTime) {
        return performVoidRequest(HttpMethod.POST, chatUrl + "/" + telegramChatId + "/notification", notificationTime);
    }

    @Override
    public ClientResponse<ListLinksResponse> getAllLinksForChat(long telegramChatId, String tag) {
        return performRequest(
                HttpMethod.GET,
                linkUrl + "/" + telegramChatId + (tag != null ? "?tag=" + tag : ""),
                null,
                ListLinksResponse.class);
    }

    @Override
    public ClientResponse<LinkResponse> addLinkForChat(long telegramChatId, AddLinkRequest link) {
        return performRequest(HttpMethod.POST, linkUrl + "/" + telegramChatId, link, LinkResponse.class);
    }

    @Override
    public ClientResponse<LinkResponse> removeLinkForChat(long telegramChatId, RemoveLinkRequest link) {
        return performRequest(HttpMethod.DELETE, linkUrl + "/" + telegramChatId, link, LinkResponse.class);
    }

    private <T> ClientResponse<T> performRequest(
            HttpMethod method, String uri, Object body, Class<T> successResponseType) {
        Object result = webClient
                .method(method)
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body != null ? BodyInserters.fromValue(body) : BodyInserters.empty())
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(successResponseType).cast(Object.class);
                    }
                    return response.bodyToMono(ApiErrorResponse.class)
                            .flatMap(error -> Mono.error(new ScrapperApiException(error.description())));
                })
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(CallNotPermittedException.class, ex -> Mono.just(RETRY))
                .onErrorResume(ScrapperApiException.class, ex -> Mono.just(RETRY))
                .block();

        if (successResponseType.isInstance(result)) {
            return new ClientResponse<>(successResponseType.cast(result), null);
        }
        return new ClientResponse<>(null, (ApiErrorResponse) result);
    }

    private ClientResponse<Void> performVoidRequest(HttpMethod method, String uri, Object body) {
        Object result = webClient
                .method(method)
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body != null ? BodyInserters.fromValue(body) : BodyInserters.empty())
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(Void.class).cast(Object.class);
                    }
                    return response.bodyToMono(ApiErrorResponse.class)
                            .flatMap(error -> Mono.error(new ScrapperApiException(error.description())));
                })
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(CallNotPermittedException.class, ex -> Mono.just(RETRY))
                .onErrorResume(ScrapperApiException.class, ex -> Mono.just(RETRY))
                .block();

        if (result == null) {
            return new ClientResponse<>(null, null);
        }
        return new ClientResponse<>(null, (ApiErrorResponse) result);
    }
}
