package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.request.LinkUpdateRequest;

public interface BotUpdateSender {
    void sendUpdate(LinkUpdateRequest linkUpdate, boolean immediate);
}
