package backend.academy.scrapper.dto.response.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowQuestionWrapper {
    @JsonProperty("items")
    private List<StackOverflowQuestion> items;
}
