package backend.academy.scrapper.exception;

public class UnknownLinkTypeException extends RuntimeException {
    public UnknownLinkTypeException(String link) {
        super("Ссылка " + link + " имеет неизвестный или неподдерживаемый формат");
    }
}
