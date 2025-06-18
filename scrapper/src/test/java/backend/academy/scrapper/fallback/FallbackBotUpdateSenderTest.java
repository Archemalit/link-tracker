package backend.academy.scrapper.fallback;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import backend.academy.scrapper.config.CircuitBreakerConfiguration;
import backend.academy.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.scrapper.service.impl.sender.FallbackBotUpdateSender;
import backend.academy.scrapper.service.impl.sender.HttpBotUpdateSender;
import backend.academy.scrapper.service.impl.sender.QueueBotUpdateSender;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import java.lang.reflect.Field;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {FallbackBotUpdateSender.class, CircuitBreakerConfiguration.class})
class FallbackBotUpdateSenderTest {
    private static final LinkUpdateRequest mockRequest =
            new LinkUpdateRequest(1L, "https://github.com/example/test", "description");
    ;

    @Autowired
    private FallbackBotUpdateSender fallbackSender;

    @Autowired
    private CircuitBreaker circuitBreaker;

    @MockitoBean
    private QueueBotUpdateSender kafkaSender;

    @MockitoBean
    private HttpBotUpdateSender httpSender;

    @AfterEach
    void resetCircuitBreaker() {
        circuitBreaker.reset();
    }

    private void setPrimaryTransport(FallbackBotUpdateSender sender, String transport) {
        try {
            Field field = FallbackBotUpdateSender.class.getDeclaredField("primaryTransport");
            field.setAccessible(true);
            field.set(sender, transport);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldUseKafka_whenKafkaIsPrimary_andKafkaSucceeds() {
        // GIVEN
        setPrimaryTransport(fallbackSender, "kafka");

        // WHEN
        fallbackSender.sendUpdate(mockRequest, true);

        // THEN
        verify(kafkaSender).sendUpdate(any(), eq(true));
        verifyNoInteractions(httpSender);
    }

    @Test
    void shouldFallbackToHttp_whenKafkaIsPrimary_andKafkaFails() {
        // GIVEN
        setPrimaryTransport(fallbackSender, "kafka");
        doThrow(new RuntimeException("Kafka error")).when(kafkaSender).sendUpdate(any(), anyBoolean());

        // WHEN
        fallbackSender.sendUpdate(mockRequest, true);

        // THEN
        verify(kafkaSender).sendUpdate(any(), eq(true));
        verify(httpSender).sendUpdate(any(), eq(true));
    }

    @Test
    void shouldUseHttp_whenHttpIsPrimary_andHttpSucceeds() {
        // GIVEN
        setPrimaryTransport(fallbackSender, "http");

        // WHEN
        fallbackSender.sendUpdate(mockRequest, true);

        // THEN
        verify(httpSender).sendUpdate(any(), eq(true));
        verifyNoInteractions(kafkaSender);
    }

    @Test
    void shouldFallbackToKafka_whenHttpIsPrimary_andHttpFails() {
        // GIVEN
        setPrimaryTransport(fallbackSender, "http");
        doThrow(new RuntimeException("HTTP error")).when(httpSender).sendUpdate(any(), anyBoolean());

        // WHEN
        fallbackSender.sendUpdate(mockRequest, true);

        // THEN
        verify(httpSender).sendUpdate(any(), eq(true));
        verify(kafkaSender).sendUpdate(any(), eq(true));
    }

    @Test
    void shouldNotCrash_whenBothSendersFail() {
        // GIVEN
        setPrimaryTransport(fallbackSender, "kafka");
        doThrow(new RuntimeException("Kafka error")).when(kafkaSender).sendUpdate(any(), anyBoolean());
        doThrow(new RuntimeException("HTTP error")).when(httpSender).sendUpdate(any(), anyBoolean());

        // EXPECT
        assertThatCode(() -> fallbackSender.sendUpdate(mockRequest, true)).doesNotThrowAnyException();
        verify(kafkaSender).sendUpdate(any(), anyBoolean());
        verify(httpSender).sendUpdate(any(), anyBoolean());
    }
}
