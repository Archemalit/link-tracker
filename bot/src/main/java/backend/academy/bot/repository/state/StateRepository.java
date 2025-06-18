package backend.academy.bot.repository.state;

public interface StateRepository {
    void setState(long telegramChatId, State state);

    State getState(long telegramChatId);

    boolean contains(long telegramChatId);

    void remove(long telegramChatId);
}
