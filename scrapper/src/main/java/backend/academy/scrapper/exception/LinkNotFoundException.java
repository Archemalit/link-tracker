package backend.academy.scrapper.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String link) {
        super("Ссылка " + link + " не найдена в отслеживаемых");
    }
}
