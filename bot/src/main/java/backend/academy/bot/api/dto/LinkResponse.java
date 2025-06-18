package backend.academy.bot.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LinkResponse(@JsonProperty("id") Long telegramChatId, @JsonProperty("url") String url) {}
