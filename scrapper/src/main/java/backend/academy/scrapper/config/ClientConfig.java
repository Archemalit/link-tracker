package backend.academy.scrapper.config;

import backend.academy.scrapper.client.RestBotClient;
import backend.academy.scrapper.client.RestClientGitHub;
import backend.academy.scrapper.client.RestClientStackOverFlow;
import backend.academy.scrapper.client.impl.RestBotClientImpl;
import backend.academy.scrapper.client.impl.RestClientGitHubImpl;
import backend.academy.scrapper.client.impl.RestClientStackOverFlowImpl;
import backend.academy.scrapper.properties.TimeoutApiPropertiesConfig;
import backend.academy.scrapper.properties.TimeoutBotPropertiesConfig;
import io.github.resilience4j.retry.Retry;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ClientConfig {
    @Bean
    public RestClientStackOverFlow restClientStackOverFlow(
            @Value("${api.url.stackoverflow}") String baseUrl,
            ScrapperPropertiesConfig config,
            TimeoutApiPropertiesConfig timeoutProperties,
            @Qualifier("retryStackOverFlow") Retry retry) {
        ReactorClientHttpRequestFactory factory = new ReactorClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutProperties.connection()));
        factory.setReadTimeout(Duration.ofMillis(timeoutProperties.read()));

        return new RestClientStackOverFlowImpl(
                RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build(), config, retry);
    }

    @Bean
    public RestClientGitHub restClientGitHub(
            @Value("${api.url.github}") String baseUrl,
            ScrapperPropertiesConfig config,
            TimeoutApiPropertiesConfig timeoutProperties,
            @Qualifier("retryGitHub") Retry retry) {
        ReactorClientHttpRequestFactory factory = new ReactorClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutProperties.connection()));
        factory.setReadTimeout(Duration.ofMillis(timeoutProperties.read()));

        return new RestClientGitHubImpl(
                RestClient.builder()
                        .baseUrl(baseUrl)
                        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + config.githubToken())
                        .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                        .requestFactory(factory)
                        .build(),
                retry);
    }

    @Bean
    public RestBotClient restBotClient(
            @Value("${client.bot.base-url}") String baseUrl,
            @Value("${client.bot.updates}") String updates,
            TimeoutBotPropertiesConfig timeoutProperties) {
        ReactorClientHttpRequestFactory factory = new ReactorClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutProperties.connection()));
        factory.setReadTimeout(Duration.ofMillis(timeoutProperties.read()));

        return new RestBotClientImpl(
                RestClient.builder().baseUrl(baseUrl).requestFactory(factory).build(), updates);
    }
}
