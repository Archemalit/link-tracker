package backend.academy.scrapper.dto.response.stackoverflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class StackOverflowQuestion {
    @JsonProperty("title")
    private String title;

    @JsonProperty("body")
    private String body;

    @Override
    public String toString() {
        return "Заголовок: '" + title + '\'' + "\n\nТело: '" + body + '\'';
    }
}
