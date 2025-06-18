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
import backend.academy.scrapper.client.RestClientStackOverFlow;
import backend.academy.scrapper.config.ScrapperPropertiesConfig;
import backend.academy.scrapper.dto.response.ApiErrorResponse;
import backend.academy.scrapper.dto.response.stackoverflow.Answer;
import backend.academy.scrapper.dto.response.stackoverflow.Owner;
import backend.academy.scrapper.dto.response.stackoverflow.StackOverflowAnswersWrapper;
import backend.academy.scrapper.exception.stackoverflow.StackOverFlowNotFound;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"app.stack-overflow.key=test", "api.url.stackoverflow=http://localhost:8080"})
@WireMockTest(httpPort = 8080)
public class RestClientStackOverFlowTest extends IntegrationEnvironment {

    @Autowired
    private ScrapperPropertiesConfig scrapperProperties;

    @Autowired
    private RestClientStackOverFlow restClientStackOverFlow;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Успешное получение списка ответов с StackOverflow")
    void shouldReturnAnswersSuccessfully() throws JsonProcessingException {
        // GIVEN
        List<Answer> mockAnswers = List.of(
                new Answer(1000L, "First Answer", new Owner("First Owner")),
                new Answer(1001L, "Second Answer", new Owner("Second Owner")));
        StackOverflowAnswersWrapper mockAnswer = new StackOverflowAnswersWrapper(mockAnswers, false);
        String questionId = "123";
        stubFor(get(urlPathEqualTo("/" + questionId + "/answers"))
                .withQueryParam(
                        "key", equalTo(scrapperProperties.stackOverflow().key()))
                .withQueryParam("site", equalTo("stackoverflow"))
                .withQueryParam("order", equalTo("desc"))
                .withQueryParam("sort", equalTo("creation"))
                .withQueryParam("filter", equalTo("withbody"))
                .withQueryParam("pagesize", equalTo("20"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockAnswer))));

        // WHEN
        List<Answer> answers = restClientStackOverFlow.getNewAnswers(questionId, null);

        // THEN
        assertThat(answers).hasSize(2);
        assertThat(answers.get(0).getOwnerDisplayName()).isEqualTo("First Owner");
        verify(1, getRequestedFor(urlPathEqualTo("/" + questionId + "/answers")));
    }

    @Test
    @DisplayName("Не должен повторять запрос при 404 и должен выбросить StackOverFlowNotFound")
    void shouldNotRetryOn4xxAndThrowException() throws JsonProcessingException {
        // GIVEN
        String questionId = "123";
        String exceptionMessage = "";
        ApiErrorResponse error = new ApiErrorResponse(
                "Вопрос или ответы не найдены: 123", "404", "GitHubRepositoryNotFound", exceptionMessage, List.of());

        stubFor(get(urlPathEqualTo("/" + questionId + "/answers"))
                .withQueryParam(
                        "key", equalTo(scrapperProperties.stackOverflow().key()))
                .withQueryParam("site", equalTo("stackoverflow"))
                .withQueryParam("order", equalTo("desc"))
                .withQueryParam("sort", equalTo("creation"))
                .withQueryParam("filter", equalTo("withbody"))
                .withQueryParam("pagesize", equalTo("20"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(error))));

        // EXPECT
        assertThatThrownBy(() -> restClientStackOverFlow.getNewAnswers(questionId, null))
                .isInstanceOf(StackOverFlowNotFound.class)
                .hasMessageContaining("Вопрос или ответы не найдены: " + questionId);

        verify(1, getRequestedFor(urlPathEqualTo("/" + questionId + "/answers")));
    }

    @Test
    @DisplayName("Повторяет запрос при ошибках 500 и успешно получает ответы с третьей попытки")
    void shouldRetryAndReturnIssuesSuccessfully() throws JsonProcessingException {
        // GIVEN
        List<Answer> mockAnswers = List.of(
                new Answer(1000L, "First Answer", new Owner("First Owner")),
                new Answer(1001L, "Second Answer", new Owner("Second Owner")));
        StackOverflowAnswersWrapper mockAnswer = new StackOverflowAnswersWrapper(mockAnswers, false);
        String questionId = "123";

        stubFor(get(urlPathEqualTo("/" + questionId + "/answers"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("SecondTry"));

        stubFor(get(urlPathEqualTo("/" + questionId + "/answers"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("SecondTry")
                .willReturn(aResponse().withStatus(500))
                .willSetStateTo("ThirdTry"));

        stubFor(get(urlPathEqualTo("/" + questionId + "/answers"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("ThirdTry")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(mockAnswer))));

        // WHEN
        List<Answer> answers = restClientStackOverFlow.getNewAnswers(questionId, null);

        // THEN
        assertThat(answers).hasSize(2);
        assertThat(answers.get(0).getOwnerDisplayName()).isEqualTo("First Owner");
        verify(3, getRequestedFor(urlPathEqualTo("/" + questionId + "/answers")));
    }
}
