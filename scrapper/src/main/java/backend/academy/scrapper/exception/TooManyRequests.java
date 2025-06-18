package backend.academy.scrapper.exception;

public class TooManyRequests extends RuntimeException {
    public TooManyRequests(String message) {
        super("Превышено количество запросов по ip адресу: " + message);
    }
}
