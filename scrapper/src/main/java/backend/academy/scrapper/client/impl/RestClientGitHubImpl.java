package backend.academy.scrapper.client.impl;

import backend.academy.scrapper.client.RestClientGitHub;
import backend.academy.scrapper.dto.response.github.GitHubResponse;
import backend.academy.scrapper.exception.github.GitHubApiCallException;
import backend.academy.scrapper.exception.github.GitHubRepositoryNotFound;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.Retry;
import io.netty.handler.timeout.ReadTimeoutException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@RequiredArgsConstructor
public class RestClientGitHubImpl implements RestClientGitHub {
    private static final long DEFAULT_TIME = 10000000L;
    private final RestClient restClient;
    private final Retry retry;

    @Override
    public List<GitHubResponse> getIssueInfo(
            String owner, String repos, LocalDateTime lastPullRequestDate, Integer perPage) {

        Supplier<List<GitHubResponse>> retryRequest = () -> {
            // TODO: тут наверное логировать всё это надо, что типо была попатка 1 из 3 и т.д.
            try {
                return restClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/" + owner + "/" + repos + "/issues")
                                .queryParam("state", "open")
                                .queryParam("sort", "created")
                                .queryParam("direction", "desc")
                                .queryParam("per_page", perPage)
                                .queryParam(
                                        "since",
                                        lastPullRequestDate != null
                                                ? lastPullRequestDate
                                                        .atOffset(ZoneOffset.UTC)
                                                        .plusSeconds(1)
                                                        .format(DateTimeFormatter.ISO_INSTANT)
                                                : null)
                                .build())
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                            String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                            throw new GitHubRepositoryNotFound(
                                    "Репозиторий не найден: " + owner + "/" + repos + "\n" + body);
                        })
                        .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                            throw new GitHubApiCallException("Ошибка сервера GitHub: " + response.getStatusCode());
                        })
                        .body(new ParameterizedTypeReference<List<GitHubResponse>>() {});
            } catch (RestClientException ex) {
                Throwable exCause = ex.getCause().getCause();
                if (exCause instanceof ReadTimeoutException || exCause instanceof SocketTimeoutException) {
                    throw new GitHubApiCallException("Таймаут при обращении к GitHub: " + ex.getMessage());
                } else {
                    throw new GitHubApiCallException("Ошибка при вызове GitHub API");
                }
            }
        };

        return Decorators.ofSupplier(retryRequest).withRetry(retry).get();
    }

    @Override
    public LocalDateTime getLastAction(String owner, String repos) {
        List<GitHubResponse> response = getIssueInfo(owner, repos, null, 1);
        if (!response.isEmpty()) {
            return response.getFirst().createdAt().atOffset(ZoneOffset.UTC).toLocalDateTime();
        } else {
            return Instant.ofEpochSecond(DEFAULT_TIME).atOffset(ZoneOffset.UTC).toLocalDateTime();
        }
    }
}
