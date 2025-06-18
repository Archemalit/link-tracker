package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.response.github.GitHubResponse;
import java.time.LocalDateTime;
import java.util.List;

public interface RestClientGitHub {
    LocalDateTime getLastAction(String owner, String repos);

    List<GitHubResponse> getIssueInfo(String owner, String repos, LocalDateTime lastPullRequestDate, Integer perPage);
}
