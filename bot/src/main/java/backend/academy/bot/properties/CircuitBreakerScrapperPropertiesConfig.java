package backend.academy.bot.properties;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.circuit-breaker.scrapper")
public record CircuitBreakerScrapperPropertiesConfig(
        @Positive int slidingWindowSize,
        @Positive int minimumNumberOfCalls,
        @Positive int failureRateThreshold,
        @Positive int waitDurationInOpenState,
        @Positive int permittedNumberOfCallsInHalfOpenState) {}
