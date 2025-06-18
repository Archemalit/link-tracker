package backend.academy.scrapper;

import backend.academy.scrapper.config.KafkaProducerPropertiesConfig;
import backend.academy.scrapper.config.KafkaUpdateTopicPropertiesConfig;
import backend.academy.scrapper.config.ScrapperPropertiesConfig;
import backend.academy.scrapper.properties.RateLimiterPropertiesConfig;
import backend.academy.scrapper.properties.RetryPropertiesConfig;
import backend.academy.scrapper.properties.TimeoutApiPropertiesConfig;
import backend.academy.scrapper.properties.TimeoutBotPropertiesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties({
    ScrapperPropertiesConfig.class,
    KafkaProducerPropertiesConfig.class,
    KafkaUpdateTopicPropertiesConfig.class,
    TimeoutApiPropertiesConfig.class,
    TimeoutBotPropertiesConfig.class,
    RetryPropertiesConfig.class,
    RateLimiterPropertiesConfig.class
})
@EnableScheduling
@EnableCaching
@EnableRetry
public class ScrapperApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScrapperApplication.class, args);
    }
}
