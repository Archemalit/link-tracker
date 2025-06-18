package backend.academy.bot.config;

import backend.academy.bot.properties.BotConfig;
import com.pengrad.telegrambot.TelegramBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
    @Bean
    public TelegramBot telegramBot(BotConfig config) {
        return new TelegramBot(config.telegramToken());
    }
}
