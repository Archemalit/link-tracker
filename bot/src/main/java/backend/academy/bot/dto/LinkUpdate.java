package backend.academy.bot.dto;

public record LinkUpdate(Long telegramChatId, String url, String description) {}
