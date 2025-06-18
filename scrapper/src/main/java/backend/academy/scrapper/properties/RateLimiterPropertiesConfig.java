package backend.academy.scrapper.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(value = "app.rate-limiter.scrapper")
@ConditionalOnProperty(name = "app.rate-limiter.scrapper.enabled", havingValue = "true", matchIfMissing = true)
public record RateLimiterPropertiesConfig(
        @Positive int limitForPeriod, @Positive int limitRefreshPeriod, @Min(0) int timeoutDuration) {}
