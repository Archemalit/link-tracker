// package backend.academy.bot.circuitbreaker;
//
// import backend.academy.bot.api.client.ScrapperClient;
// import backend.academy.bot.api.dto.ApiErrorResponse;
// import backend.academy.bot.api.dto.ClientResponse;
// import backend.academy.bot.config.CircuitBreakerConfiguration;
// import backend.academy.bot.config.ClientConfig;
// import backend.academy.bot.properties.ScrapperClientPropertiesConfig;
// import com.github.tomakehurst.wiremock.junit5.WireMockTest;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.TestPropertySource;
// import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
// import static com.github.tomakehurst.wiremock.client.WireMock.post;
// import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
// import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
// import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
// import static com.github.tomakehurst.wiremock.client.WireMock.verify;
// import static org.assertj.core.api.Assertions.assertThat;
//
// @SpringBootTest(classes = {ClientConfig.class, CircuitBreakerConfiguration.class})
// @TestPropertySource(properties = {
//    "client.scrapper.base-url=http://localhost:8085",
//    "app.circuit-breaker.scrapper.sliding-window-size=10",
//    "app.circuit-breaker.scrapper.minimum-number-of-calls=10",
//    "app.circuit-breaker.scrapper.failure-rate-threshold=50",
//    "app.circuit-breaker.scrapper.wait-duration-in-open-state=5",
//    "app.circuit-breaker.scrapper.permitted-number-of-calls-in-half-open-state=2",
// })
// @WireMockTest(httpPort = 8085)
// public class ScrapperClientImplCircuitBreakerTest {
//    @Autowired
//    private ScrapperClient scrapperClient;
//
//    @Test
//    @DisplayName("")
//    void shouldOpenCircuitBreakerAfterFailures() {
//        for (int i = 0; i < 6; i++) {
//            stubFor(post(urlEqualTo("/tg-chat/" + i)).willReturn(aResponse().withStatus(500)));
//            scrapperClient.registerChat(i);
//        }
//
//        for (int i = 0; i < 4; i++) {
//            stubFor(post(urlEqualTo("/tg-chat/" + i)).willReturn(aResponse().withStatus(200)));
//            scrapperClient.registerChat(i);
//        }
//
//        ClientResponse<Void> response = scrapperClient.registerChat(999);
//        assertThat(response.error()).isNotNull();
//        assertThat(response.error().description()).contains("Service temporarily unavailable");
//
//        verify(0, postRequestedFor(urlEqualTo("/tg-chat/999")));
//    }
// }
