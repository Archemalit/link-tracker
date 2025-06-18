package backend.academy.bot.properties;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.timeout.scrapper")
public record TimeoutScrapperPropertiesConfig(@Positive int connection, @Positive int read) {}
