package backend.academy.scrapper.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AddLinkRequest(
        @JsonProperty("link") String link,
        @JsonProperty("tag") String tag,
        @JsonProperty("filters") List<String> filters) {}
