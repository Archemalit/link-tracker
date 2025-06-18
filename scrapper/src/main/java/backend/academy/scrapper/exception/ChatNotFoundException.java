package backend.academy.scrapper.exception;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException(long telegramChatId) {
        super("Чат с id " + telegramChatId + " не найден");
    }
}
