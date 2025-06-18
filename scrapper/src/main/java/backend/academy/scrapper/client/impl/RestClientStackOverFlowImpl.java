package backend.academy.scrapper.client.impl;

import backend.academy.scrapper.client.RestClientStackOverFlow;
import backend.academy.scrapper.config.ScrapperPropertiesConfig;
import backend.academy.scrapper.dto.response.stackoverflow.Answer;
import backend.academy.scrapper.dto.response.stackoverflow.StackOverflowAnswersWrapper;
import backend.academy.scrapper.exception.stackoverflow.StackOverFlowApiCallException;
import backend.academy.scrapper.exception.stackoverflow.StackOverFlowNotFound;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.netty.handler.timeout.ReadTimeoutException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;

@RequiredArgsConstructor
public class RestClientStackOverFlowImpl implements RestClientStackOverFlow {
    private static final int PAGE_SIZE = 20;
    private static final long DEFAULT_TIME = 10000000L;
    private final RestClient restClient;
    private final ScrapperPropertiesConfig config;
    private final Retry retry;

    @Override
    public Long getLastAnswerDate(String questionId) {
        Supplier<Long> retryRequest = () -> {
            try {
                StackOverflowAnswersWrapper responseAnswer = restClient
                        .get()
                        .uri(uriBuilder -> setMainURIParams(uriBuilder)
                                .path("/" + questionId + "/answers")
                                .queryParam("pagesize", 1)
                                .build())
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                            throw new StackOverFlowNotFound("Не найден ресурс: " + questionId);
                        })
                        .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                            throw new StackOverFlowApiCallException("Ошибка сервера Stack Overflow");
                        })
                        .body(StackOverflowAnswersWrapper.class);

                if (responseAnswer.items() == null || responseAnswer.items().isEmpty()) {
                    return DEFAULT_TIME;
                }

                return responseAnswer.items().getFirst().creationDate();

            } catch (RestClientException ex) {
                Throwable exCause = ex.getCause().getCause();
                if (exCause instanceof ReadTimeoutException || exCause instanceof SocketTimeoutException) {
                    throw new StackOverFlowApiCallException(
                            "Таймаут при обращении к Stack Overflow API: " + ex.getMessage());
                } else {
                    throw new StackOverFlowApiCallException("Ошибка при вызове Stack Overflow API");
                }
            }
        };

        return Decorators.ofSupplier(retryRequest).withRetry(retry).get();
    }

    @Override
    public List<Answer> getNewAnswers(String questionId, LocalDateTime lastAnswerDate) {
        Supplier<List<Answer>> retryRequest = () -> {
            List<Answer> answerResponses = new ArrayList<>();
            int count = 1;

            while (true) {
                try {
                    int finalCount = count;
                    StackOverflowAnswersWrapper responseAnswer = restClient
                            .get()
                            .uri(uriBuilder -> setMainURIParams(uriBuilder)
                                    .path("/" + questionId + "/answers")
                                    .queryParam("pagesize", PAGE_SIZE)
                                    .queryParam("page", String.valueOf(finalCount))
                                    .build())
                            .retrieve()
                            .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                                if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                                    throw new StackOverFlowNotFound("Вопрос или ответы не найдены: " + questionId);
                                } else {
                                    throw new StackOverFlowApiCallException(
                                            "Ошибка клиента при обращении к Stack Overflow API");
                                }
                            })
                            .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                                throw new StackOverFlowApiCallException(
                                        "Ошибка сервера Stack Overflow: " + response.getStatusCode());
                            })
                            .body(StackOverflowAnswersWrapper.class);

                    if (responseAnswer.items() == null || responseAnswer.items().isEmpty()) {
                        break;
                    }

                    for (Answer answer : responseAnswer.items()) {
                        if (lastAnswerDate == null
                                || answer.creationDate() > lastAnswerDate.toEpochSecond(ZoneOffset.UTC)) {
                            answerResponses.add(answer);
                        } else {
                            return answerResponses;
                        }
                    }

                    if (!responseAnswer.hasMore()) {
                        break;
                    }
                    count++;
                } catch (RestClientException ex) {
                    Throwable exCause = ex.getCause().getCause();
                    if (exCause instanceof ReadTimeoutException || exCause instanceof SocketTimeoutException) {
                        throw new StackOverFlowApiCallException(
                                "Таймаут при обращении к Stack Overflow API: " + ex.getMessage());
                    } else {
                        throw new StackOverFlowApiCallException("Ошибка при вызове Stack Overflow API");
                    }
                }
            }

            return answerResponses;
        };

        return Decorators.ofSupplier(retryRequest).withRetry(retry).get();
    }

    private UriBuilder setMainURIParams(UriBuilder builder) {
        return builder.queryParam("key", config.stackOverflow().key())
                .queryParam("site", "stackoverflow")
                .queryParam("order", "desc")
                .queryParam("sort", "creation")
                .queryParam("filter", "withbody");
    }
}
