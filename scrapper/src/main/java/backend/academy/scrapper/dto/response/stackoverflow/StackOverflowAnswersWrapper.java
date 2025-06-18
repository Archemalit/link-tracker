package backend.academy.scrapper.dto.response.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class StackOverflowAnswersWrapper {
    @JsonProperty("items")
    private List<Answer> items;

    @JsonProperty("has_more")
    private boolean hasMore;
}
