package backend.academy.scrapper.service;

import backend.academy.scrapper.dto.request.AddNotificationTimeRequest;

public interface ChatService {
    void registerChat(long telegramChatId);

    void deleteChat(long telegramChatId);

    void addNotificationType(long telegramChatId, AddNotificationTimeRequest addNotificationTimeRequest);
}
