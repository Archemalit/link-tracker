package backend.academy.scrapper.exception;

public class NotFoundIpException extends RuntimeException {
    public NotFoundIpException(String message) {
        super(message);
    }
}
