package backend.academy.bot.service.bot;

import backend.academy.bot.api.dto.AddLinkRequest;
import backend.academy.bot.api.dto.AddNotificationTimeRequest;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.api.dto.LinkResponse;
import backend.academy.bot.api.dto.ListLinksResponse;
import backend.academy.bot.api.dto.RemoveLinkRequest;

public interface BotService {
    ClientResponse<Void> registerUser(long telegramChatId);

    ClientResponse<Void> addNotificationType(long telegramChatId, AddNotificationTimeRequest notificationTime);

    ClientResponse<ListLinksResponse> getAllLinksForChat(long telegramChatId, String tag);

    ClientResponse<LinkResponse> addLinkForChat(long telegramChatId, AddLinkRequest link);

    ClientResponse<LinkResponse> removeLinkForChat(long telegramChatId, RemoveLinkRequest link);
}
