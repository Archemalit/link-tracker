package backend.academy.scrapper.dto.response.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GitHubErrorResponse {
    private String message;

    @JsonProperty("documentation_url")
    private String documentationUrl;

    private String status;
}
