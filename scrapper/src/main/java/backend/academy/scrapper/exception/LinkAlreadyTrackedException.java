package backend.academy.scrapper.exception;

public class LinkAlreadyTrackedException extends RuntimeException {
    public LinkAlreadyTrackedException(String link) {
        super("Ссылка " + link + " уже добавлена в список отслеживаемых");
    }
}
