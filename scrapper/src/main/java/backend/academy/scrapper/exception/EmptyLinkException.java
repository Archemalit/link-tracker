package backend.academy.scrapper.exception;

public class EmptyLinkException extends RuntimeException {
    public EmptyLinkException() {
        super("Ссылка не была передана в запросе");
    }
}
