package backend.academy.scrapper.config;

import backend.academy.scrapper.properties.CircuitBreakerSenderPropertiesConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({CircuitBreakerSenderPropertiesConfig.class})
public class CircuitBreakerConfiguration {
    @Bean
    public CircuitBreaker circuitBreakerConfig(CircuitBreakerSenderPropertiesConfig circuitBreakerProperties) {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(circuitBreakerProperties.slidingWindowSize())
                .minimumNumberOfCalls(circuitBreakerProperties.minimumNumberOfCalls())
                .failureRateThreshold(circuitBreakerProperties.failureRateThreshold())
                .waitDurationInOpenState(Duration.ofSeconds(circuitBreakerProperties.waitDurationInOpenState()))
                .permittedNumberOfCallsInHalfOpenState(circuitBreakerProperties.permittedNumberOfCallsInHalfOpenState())
                .automaticTransitionFromOpenToHalfOpenEnabled(
                        circuitBreakerProperties.automaticTransitionFromOpenToHalfOpenEnabled())
                .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        return registry.circuitBreaker("sender");
    }
}
