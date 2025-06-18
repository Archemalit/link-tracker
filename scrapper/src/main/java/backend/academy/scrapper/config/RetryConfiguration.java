package backend.academy.scrapper.config;

import backend.academy.scrapper.exception.github.GitHubRepositoryNotFound;
import backend.academy.scrapper.exception.stackoverflow.StackOverFlowNotFound;
import backend.academy.scrapper.properties.RetryPropertiesConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RetryConfiguration {

    @Bean
    public Retry retryGitHub(RetryPropertiesConfig retryProperties) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(retryProperties.maxAttempts())
                .waitDuration(Duration.ofMillis(retryProperties.backoff()))
                .ignoreExceptions(GitHubRepositoryNotFound.class)
                .failAfterMaxAttempts(true)
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        return registry.retry("github");
    }

    @Bean
    public Retry retryStackOverFlow(RetryPropertiesConfig retryProperties) {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(retryProperties.maxAttempts())
                .waitDuration(Duration.ofMillis(retryProperties.backoff()))
                .ignoreExceptions(StackOverFlowNotFound.class)
                .failAfterMaxAttempts(true)
                .build();

        RetryRegistry registry = RetryRegistry.of(config);
        return registry.retry("stackoverflow");
    }
}
