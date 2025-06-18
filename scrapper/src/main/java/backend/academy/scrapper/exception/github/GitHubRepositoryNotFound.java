package backend.academy.scrapper.exception.github;

public class GitHubRepositoryNotFound extends RuntimeException {
    public GitHubRepositoryNotFound(String message) {
        super(message);
    }
}
