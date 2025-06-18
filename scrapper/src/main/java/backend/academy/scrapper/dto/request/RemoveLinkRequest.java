package backend.academy.scrapper.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RemoveLinkRequest(@JsonProperty("link") String link) {}
