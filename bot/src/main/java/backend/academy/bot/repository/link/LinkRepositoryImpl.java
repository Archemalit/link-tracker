package backend.academy.bot.repository.link;

import backend.academy.bot.api.dto.AddLinkRequest;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class LinkRepositoryImpl implements LinkRepository {
    private final Map<Long, AddLinkRequest> linkForChats = new ConcurrentHashMap<>();

    @Override
    public void addLink(long telegramChatId, String link) {
        linkForChats.put(telegramChatId, AddLinkRequest.builder().link(link).build());
    }

    @Override
    public void addTag(long telegramChatId, String tag) {
        linkForChats.get(telegramChatId).setTag(tag);
    }

    @Override
    public void addFilters(long telegramChatId, List<String> filters) {
        linkForChats.get(telegramChatId).setFilters(filters);
    }

    @Override
    public AddLinkRequest getLink(long telegramChatId) {
        return linkForChats.get(telegramChatId);
    }

    @Override
    public void remove(long telegramChatId) {
        linkForChats.remove(telegramChatId);
    }
}
