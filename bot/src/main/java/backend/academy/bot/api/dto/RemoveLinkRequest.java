package backend.academy.bot.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RemoveLinkRequest(@JsonProperty("link") String link) {}
