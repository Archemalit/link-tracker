package backend.academy.scrapper.properties;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.timeout.bot")
public record TimeoutBotPropertiesConfig(@Positive int connection, @Positive int read) {}
