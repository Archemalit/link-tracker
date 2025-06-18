package backend.academy.scrapper.dto.response.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
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
public class GitHubResponse {
    @Getter
    @JsonProperty("title")
    private String title;

    @JsonProperty("user")
    private User user;

    @Getter
    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("body")
    private String body;

    public String getUserName() {
        return user != null ? user.login() : "Unknown";
    }

    public String getPreview() {
        return (body != null && body.length() > 200) ? body.substring(0, 200) + "..." : body;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        @JsonProperty("login")
        private String login;
    }

    @Override
    public String toString() {
        return "Title: " + title + "\n" + "User: "
                + getUserName() + "\n" + "Created At: "
                + createdAt + "\n" + "Preview: "
                + getPreview() + "\n";
    }
}
