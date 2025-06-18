package backend.academy.bot.api.dto;

public record ClientResponse<T>(T response, ApiErrorResponse error) {}
