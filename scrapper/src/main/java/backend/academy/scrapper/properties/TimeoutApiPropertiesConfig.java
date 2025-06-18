package backend.academy.scrapper.properties;

import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app.timeout.api")
public record TimeoutApiPropertiesConfig(@Positive int connection, @Positive int read) {}
