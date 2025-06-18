package backend.academy.scrapper.ratelimiter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.scrapper.config.RateLimiterConfiguration;
import backend.academy.scrapper.config.WebConfig;
import backend.academy.scrapper.controller.ChatController;
import backend.academy.scrapper.interceptor.IpRateLimiterInterceptor;
import backend.academy.scrapper.service.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(ChatController.class)
@Import({RateLimiterConfiguration.class, IpRateLimiterInterceptor.class, WebConfig.class})
@TestPropertySource(
        properties = {
            "app.rate-limiter.scrapper.enabled=true",
            "app.rate-limiter.scrapper.limit-for-period=2",
            "app.rate-limiter.scrapper.limit-refresh-period=60",
            "app.rate-limiter.scrapper.timeout-duration=0"
        })
public class IpRateLimiterInterceptorTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Test
    @DisplayName("Должен разрешить запросы в пределах лимита")
    void shouldAllowRequestWithoutRateLimit() throws Exception {
        // GIVEN
        String telegramChatId = "123";
        String ip = "127.0.0.1";

        // EXPECT
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/tg-chat/{id}", telegramChatId).header("X-Forwarded-For", ip))
                    .andExpect(status().isOk());
        }
    }

    @Test
    @DisplayName("Должен блокировать запросы при превышении лимита")
    void shouldNotAllowRequestAfterRateLimiterException() throws Exception {
        // GIVEN
        String id = "123";
        String ip = "127.0.0.2";

        // EXPECT
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/tg-chat/{id}", id).header("X-Forwarded-For", ip))
                    .andExpect(status().isOk());
        }
        mockMvc.perform(post("/tg-chat/{id}", id).header("X-Forwarded-For", ip))
                .andExpect(status().isTooManyRequests());
    }
}
