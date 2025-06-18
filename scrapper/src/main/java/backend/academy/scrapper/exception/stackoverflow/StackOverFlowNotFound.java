package backend.academy.scrapper.exception.stackoverflow;

public class StackOverFlowNotFound extends RuntimeException {
    public StackOverFlowNotFound(String message) {
        super(message);
    }
}
