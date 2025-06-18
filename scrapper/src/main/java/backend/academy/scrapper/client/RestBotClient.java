package backend.academy.scrapper.client;

import backend.academy.scrapper.dto.request.LinkUpdateRequest;

public interface RestBotClient {
    void notifyUser(LinkUpdateRequest linkUpdate);
}
