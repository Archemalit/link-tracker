package backend.academy.scrapper.retry;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.scrapper.IntegrationEnvironment;
import backend.academy.scrapper.client.RestClientGitHub;
import backend.academy.scrapper.config.ScrapperPropertiesConfig;
import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.dto.response.github.GitHubResponse;
import backend.academy.scrapper.exception.github.GitHubRepositoryNotFound;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"app.github-token=test", "api.url.github=http://localhost:8080"})
@WireMockTest(httpPort = 8080)
public class RestClientGitHubTest extends IntegrationEnvironment {

    @Autowired
    private ScrapperPropertiesConfig scrapperProperties;

    @Autowired
    private RestClientGitHub restClientGitHub;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Должен успешно вернуть список задач с GitHub")
    void shouldReturnIssuesSuccessfully() throws Exception {
        // GIVEN
        List<GitHubResponse> mockResponse = List.of(
                new GitHubResponse("Issue 1", null, null, null), new GitHubResponse("Issue 2", null, null, null));

        stubFor(get(urlPathEqualTo("/owner/repo/issues"))
                .withQueryParam("state", equalTo("open"))
                .withQueryParam("sort", equalTo("created"))
                .withQueryParam("direction", equalTo("desc"))
                .withQueryParam("per_page", equalTo("10"))
                .withQueryParam("since", equalTo(""))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + scrapperProperties.githubToken()))
                .withHeader(HttpHeaders.ACCEPT, equalTo("application/vnd.github+json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // WHEN
        List<GitHubResponse> result = restClientGitHub.getIssueInfo("owner", "repo", null, 10);

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Issue 1");
    }

    @Test
    @DisplayName("Не должен делать retry при 4xx и должен выбросить исключение GitHubRepositoryNotFound")
    void shouldNotRetryOn4xxAndThrowException() throws JsonProcessingException {
        // GIVEN
        String exceptionMessage =
                """
            Репозиторий не найден: owner/repo
            {"message":"Not Found","documentation_url":"https://docs.github.com/rest/issues/issues#list-repository-issues","status":"404"}
            """;
        ApiErrorResponse error = new ApiErrorResponse(
                "Репозиторий не найден", "404", "GitHubRepositoryNotFound", exceptionMessage, List.of());
        stubFor(get(urlPathEqualTo("/owner/repo/issues"))
                .withQueryParam("state", equalTo("open"))
                .withQueryParam("sort", equalTo("created"))
                .withQueryParam("direction", equalTo("desc"))
                .withQueryParam("per_page", equalTo("10"))
                .withQueryParam("since", equalTo(""))
                .withHeader(HttpHeaders.AUTHORIZATION, equalTo("Bearer " + scrapperProperties.githubToken()))
                .withHeader(HttpHeaders.ACCEPT, equalTo("application/vnd.github+json"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(error))));

        // EXPECT
        assertThatThrownBy(() -> restClientGitHub.getIssueInfo("owner", "repo", null, 10))
                .isInstanceOf(GitHubRepositoryNotFound.class)
                .hasMessageContaining("Репозиторий не найден");

        verify(1, getRequestedFor(urlPathEqualTo("/owner/repo/issues")));
    }

    @Test
    @DisplayName("Должен сделать retry и успешно вернуть список задач после ошибок 500")
    void shouldRetryAndReturnIssuesSuccessfully() throws JsonProcessingException {
        // GIVEN
        List<GitHubResponse> mockResponse = List.of(
                new GitHubResponse("Issue 1", null, null, null), new GitHubResponse("Issue 2", null, null, null));

        stubFor(get(urlPathEqualTo("/owner/repo/issues"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("SecondTry"));

        stubFor(get(urlPathEqualTo("/owner/repo/issues"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("SecondTry")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("ThirdTry"));

        stubFor(get(urlPathEqualTo("/owner/repo/issues"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("ThirdTry")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // WHEN
        List<GitHubResponse> result = restClientGitHub.getIssueInfo("owner", "repo", null, 10);

        // THEN
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("Issue 1");

        verify(3, getRequestedFor(urlPathEqualTo("/owner/repo/issues")));
    }
}
