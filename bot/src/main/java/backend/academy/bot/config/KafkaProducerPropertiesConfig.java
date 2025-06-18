package backend.academy.bot.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("kafka.producer")
public record KafkaProducerPropertiesConfig(
        @NotEmpty String bootstrapServers,
        @NotEmpty String clientId,
        @NotEmpty String acksMode,
        Duration deliveryTimeout,
        @Min(1) Integer lingerMs,
        @Min(1) Integer batchSize) {}
