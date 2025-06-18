package backend.academy.scrapper.config;

import backend.academy.scrapper.properties.RateLimiterPropertiesConfig;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.rate-limiter.scrapper.enabled", havingValue = "true", matchIfMissing = true)
public class RateLimiterConfiguration {
    @Bean
    public RateLimiterConfig rateLimiterConfig(RateLimiterPropertiesConfig rateLimiterProperties) {
        return RateLimiterConfig.custom()
                .limitForPeriod(rateLimiterProperties.limitForPeriod())
                .limitRefreshPeriod(Duration.ofSeconds(rateLimiterProperties.limitRefreshPeriod()))
                .timeoutDuration(Duration.ofSeconds(rateLimiterProperties.timeoutDuration()))
                .build();
    }
}
