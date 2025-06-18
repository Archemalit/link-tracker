package backend.academy.scrapper.exception.github;

public class GitHubApiCallException extends RuntimeException {
    public GitHubApiCallException(String message) {
        super(message);
    }
}
