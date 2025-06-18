package backend.academy.bot.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("kafka.consumer")
public record KafkaConsumerPropertiesConfig(
        @NotEmpty String bootstrapServers,
        @NotEmpty String groupId,
        @NotEmpty String autoOffsetReset,
        @Min(1) Integer maxPollIntervalMs,
        boolean enableAutoCommit,
        @Min(1) Integer concurrency) {}
