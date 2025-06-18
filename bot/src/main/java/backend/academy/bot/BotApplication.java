package backend.academy.bot;

import backend.academy.bot.config.KafkaConsumerPropertiesConfig;
import backend.academy.bot.config.KafkaProducerPropertiesConfig;
import backend.academy.bot.config.KafkaUpdateTopicPropertiesConfig;
import backend.academy.bot.properties.BotConfig;
import backend.academy.bot.properties.TimeoutScrapperPropertiesConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
    BotConfig.class,
    KafkaUpdateTopicPropertiesConfig.class,
    KafkaConsumerPropertiesConfig.class,
    KafkaProducerPropertiesConfig.class,
    TimeoutScrapperPropertiesConfig.class,
})
public class BotApplication {
    public static void main(String[] args) {
        SpringApplication.run(BotApplication.class, args);
    }
}
