package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;

public interface LinkService {
    ListLinksResponse getAllLinksByChatIdAndTag(long telegramChatId, String tag);

    LinkResponse addLinkForChat(long telegramChatId, AddLinkRequest addLinkRequest);

    LinkResponse deleteLinkFromChat(long telegramChatId, RemoveLinkRequest removeLinkRequest);
}
