package backend.academy.scrapper.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;

import backend.academy.scrapper.IntegrationEnvironment;
import backend.academy.scrapper.controller.ChatController;
import backend.academy.scrapper.service.ChatService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
public class UserMessageMetricTest extends IntegrationEnvironment {
    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private ChatController chatController;

    @Autowired
    private UserMessageMetric userMessageMetric;

    @MockitoBean
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        meterRegistry.forEachMeter(meterRegistry::remove);
    }

    @Test
    @DisplayName("Проверка работы метрик на количество сообщений")
    void handle() {
        // GIVEN
        long telegramChatId = 1L;
        doNothing().when(chatService).registerChat(telegramChatId);

        // WHEN
        chatController.registerChat(telegramChatId);
        chatController.registerChat(telegramChatId);

        // THEN
        assertThat(userMessageMetric.count()).isEqualTo(2.0);
    }
}
