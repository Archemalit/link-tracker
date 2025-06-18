package backend.academy.bot.client;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

import backend.academy.bot.api.client.ScrapperClient;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.api.dto.LinkResponse;
import backend.academy.bot.api.dto.ListLinksResponse;
import backend.academy.bot.api.dto.RemoveLinkRequest;
import backend.academy.bot.config.CircuitBreakerConfiguration;
import backend.academy.bot.config.ClientConfig;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

// TODO: вообще, всё это по-хорошему стоит переписать на MockMvcTest
@WireMockTest(httpPort = 8089)
@SpringBootTest(classes = {ClientConfig.class, CircuitBreakerConfiguration.class})
@TestPropertySource(
        properties = {
            "client.scrapper.base-url=http://localhost:8089",
            "client.scrapper.sub-chat-url=/tg-chat",
            "client.scrapper.sub-link-url=/links"
        })
class ScrapperClientImplTest {

    @Autowired
    private ScrapperClient scrapperClient;

    @Test
    @DisplayName("Успешная регистрация чата")
    void testRegisterChatSuccess() {
        // GIVEN
        long chatId = 123;
        stubFor(post(urlEqualTo("/tg-chat/" + chatId)).willReturn(aResponse().withStatus(200)));

        // WHEN
        ClientResponse<Void> response = scrapperClient.registerChat(chatId);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.error()).isNull();
    }

    @Test
    @DisplayName("Ошибка регистрации чата (404)")
    void testRegisterChatNotFound() {
        // GIVEN
        long chatId = 123;
        stubFor(post(urlEqualTo("/tg-chat/" + chatId))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"exceptionName\": \"Not Found\", \"code\": 404}")));

        // WHEN
        ClientResponse<Void> response = scrapperClient.registerChat(chatId);

        // THEN
        assertThat(response.response()).isNull();
        assertThat(response.error()).isNotNull();
    }

    @Test
    @DisplayName("Получение списка ссылок для чата")
    void testGetAllLinksForChatSuccess() {
        // GIVEN
        long chatId = 1L;
        String tag = null;
        stubFor(get(urlEqualTo("/links/" + chatId))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"links\":[{\"id\":1,\"url\":\"https://github.com/example/test\"}],\"size\":1}")));

        // WHEN
        ClientResponse<ListLinksResponse> response = scrapperClient.getAllLinksForChat(chatId, tag);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.response().links()).isNotNull();
        assertThat(response.response().links().get(0).url()).isEqualTo("https://github.com/example/test");
    }

    @Test
    @DisplayName("Удаление ссылки из чата")
    void testRemoveLinkForChatSuccess() {
        // GIVEN
        long chatId = 1L;
        String link = "https://github.com/example/test";
        RemoveLinkRequest request = new RemoveLinkRequest(link);

        stubFor(delete(urlEqualTo("/links/" + chatId))
                .withRequestBody(equalToJson("{\"link\": \"" + link + "\"}"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":" + chatId + ",\"url\": \"" + link + "\"}")));

        // WHEN
        ClientResponse<LinkResponse> response = scrapperClient.removeLinkForChat(chatId, request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.response()).isNotNull();
        assertThat(response.response().url()).isEqualTo(link);
    }
}
