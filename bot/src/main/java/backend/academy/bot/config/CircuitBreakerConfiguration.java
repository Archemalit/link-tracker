package backend.academy.bot.config;

import backend.academy.bot.properties.CircuitBreakerScrapperPropertiesConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({CircuitBreakerScrapperPropertiesConfig.class})
public class CircuitBreakerConfiguration {
    @Bean
    public CircuitBreaker circuitBreakerConfig(CircuitBreakerScrapperPropertiesConfig circuitBreakerProperties) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(circuitBreakerProperties.slidingWindowSize())
                .minimumNumberOfCalls(circuitBreakerProperties.minimumNumberOfCalls())
                .failureRateThreshold(circuitBreakerProperties.failureRateThreshold())
                .waitDurationInOpenState(Duration.ofSeconds(circuitBreakerProperties.waitDurationInOpenState()))
                .permittedNumberOfCallsInHalfOpenState(circuitBreakerProperties.permittedNumberOfCallsInHalfOpenState())
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        return registry.circuitBreaker("scrapper");
    }
}
