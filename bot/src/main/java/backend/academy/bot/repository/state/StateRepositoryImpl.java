package backend.academy.bot.repository.state;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class StateRepositoryImpl implements StateRepository {
    private final Map<Long, State> stateForChat = new ConcurrentHashMap<>();

    @Override
    public void setState(long telegramChatId, State state) {
        stateForChat.put(telegramChatId, state);
    }

    @Override
    public State getState(long telegramChatId) {
        return stateForChat.getOrDefault(telegramChatId, State.IDLE);
    }

    @Override
    public boolean contains(long telegramChatId) {
        return stateForChat.containsKey(telegramChatId);
    }

    @Override
    public void remove(long telegramChatId) {
        stateForChat.remove(telegramChatId);
    }
}
