package backend.academy.bot.repository.link;

import backend.academy.bot.api.dto.AddLinkRequest;
import java.util.List;

public interface LinkRepository {
    void addLink(long telegramChatId, String link);

    void addTag(long telegramChatId, String tag);

    void addFilters(long telegramChatId, List<String> filters);

    AddLinkRequest getLink(long telegramChatId);

    void remove(long telegramChatId);
}
