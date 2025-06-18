package backend.academy.scrapper.exception;

public class BotNotificationFailedException extends RuntimeException {
    public BotNotificationFailedException(String message) {
        super(message);
    }
}
