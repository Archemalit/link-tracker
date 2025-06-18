package backend.academy.bot.service.bot;

import backend.academy.bot.api.client.ScrapperClient;
import backend.academy.bot.api.dto.AddLinkRequest;
import backend.academy.bot.api.dto.AddNotificationTimeRequest;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.api.dto.LinkResponse;
import backend.academy.bot.api.dto.ListLinksResponse;
import backend.academy.bot.api.dto.RemoveLinkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BotServiceImpl implements BotService {
    private final ScrapperClient scrapperClient;

    @Override
    public ClientResponse<Void> registerUser(long telegramChatId) {
        return scrapperClient.registerChat(telegramChatId);
    }

    @Override
    public ClientResponse<Void> addNotificationType(long telegramChatId, AddNotificationTimeRequest notificationTime) {
        return scrapperClient.addNotificationType(telegramChatId, notificationTime);
    }

    @Override
    public ClientResponse<ListLinksResponse> getAllLinksForChat(long telegramChatId, String tag) {
        return scrapperClient.getAllLinksForChat(telegramChatId, tag);
    }

    @Override
    public ClientResponse<LinkResponse> addLinkForChat(long telegramChatId, AddLinkRequest link) {
        return scrapperClient.addLinkForChat(telegramChatId, link);
    }

    @Override
    public ClientResponse<LinkResponse> removeLinkForChat(long telegramChatId, RemoveLinkRequest link) {
        return scrapperClient.removeLinkForChat(telegramChatId, link);
    }
}
