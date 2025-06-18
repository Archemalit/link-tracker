package backend.academy.scrapper.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LinkResponse(@JsonProperty("id") Long chatId, @JsonProperty("url") String url) {}
