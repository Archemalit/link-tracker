package backend.academy.bot.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class AddLinkRequest {
    @JsonProperty("link")
    private String link;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("filters")
    private List<String> filters;

    public String getLink() {
        return link;
    }

    public String getTag() {
        return tag;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }
}
