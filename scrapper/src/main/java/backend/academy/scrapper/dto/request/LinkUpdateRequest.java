package backend.academy.scrapper.dto.request;

public record LinkUpdateRequest(long telegramChatId, String url, String description) {}
