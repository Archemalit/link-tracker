package backend.academy.bot.config;

import backend.academy.bot.api.client.ScrapperClient;
import backend.academy.bot.api.client.ScrapperClientImpl;
import backend.academy.bot.properties.ScrapperClientPropertiesConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties({ScrapperClientPropertiesConfig.class})
public class ClientConfig {

    @Bean
    public ScrapperClient scrapperClient(
            ScrapperClientPropertiesConfig scrapperProperties, CircuitBreaker circuitBreaker) {
        return new ScrapperClientImpl(
                WebClient.builder().baseUrl(scrapperProperties.baseUrl()).build(),
                scrapperProperties.subChatUrl(),
                scrapperProperties.subLinkUrl(),
                circuitBreaker);
    }
}
