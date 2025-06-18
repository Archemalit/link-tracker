package backend.academy.bot.properties;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("client.scrapper")
public record ScrapperClientPropertiesConfig(
        @NotEmpty String baseUrl, @NotEmpty String subChatUrl, @NotEmpty String subLinkUrl) {}
