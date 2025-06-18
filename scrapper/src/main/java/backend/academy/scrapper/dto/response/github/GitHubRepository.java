package backend.academy.scrapper.dto.response.github;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GitHubRepository {
    private long id;

    @JsonProperty("node_id")
    private String nodeId;

    private String name;

    @JsonProperty("full_name")
    private String fullName;

    private boolean _private;
    private GitHubUser owner;

    @JsonProperty("html_url")
    private String htmlUrl;

    private String description;
    private boolean fork;
    private String url;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    @JsonProperty("pushed_at")
    private Instant pushedAt;

    @JsonProperty("git_url")
    private String gitUrl;

    @JsonProperty("ssh_url")
    private String sshUrl;

    @JsonProperty("clone_url")
    private String cloneUrl;

    @JsonProperty("svn_url")
    private String svnUrl;

    private String homepage;
    private int size;

    @JsonProperty("stargazers_count")
    private int stargazersCount;

    @JsonProperty("watchers_count")
    private int watchersCount;

    private String language;

    @JsonProperty("has_issues")
    private boolean hasIssues;

    @JsonProperty("has_projects")
    private boolean hasProjects;

    @JsonProperty("has_downloads")
    private boolean hasDownloads;

    @JsonProperty("has_wiki")
    private boolean hasWiki;

    @JsonProperty("has_pages")
    private boolean hasPages;

    private boolean archived;
    private boolean disabled;

    @JsonProperty("open_issues_count")
    private int openIssuesCount;

    private List<String> topics;
    private String visibility;
    private int forks;

    @JsonProperty("open_issues")
    private int openIssues;

    private int watchers;

    @JsonProperty("default_branch")
    private String defaultBranch;

    public Instant getPushedAt() {
        return pushedAt;
    }
}
