package backend.academy.bot.command;

import backend.academy.bot.api.dto.ApiErrorResponse;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.api.dto.LinkResponse;
import backend.academy.bot.api.dto.RemoveLinkRequest;
import backend.academy.bot.service.bot.BotService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Properties;
import org.springframework.stereotype.Component;

@Component
public class UnTrackCommand extends CommandHandler {
    private final BotService botService;

    public UnTrackCommand(Properties properties, BotService botService) {
        super(properties);
        this.botService = botService;
    }

    @Override
    public SendMessage handle(Update update) {
        long telegramChatId = update.message().chat().id();
        String[] data = update.message().text().trim().split("\\s+");

        if (data.length < 2) {
            return new SendMessage(telegramChatId, properties.getProperty("command.untrack.nourl"));
        }
        if (data.length > 2) {
            return new SendMessage(telegramChatId, properties.getProperty("command.untrack.manyurls"));
        }

        RemoveLinkRequest request = new RemoveLinkRequest(data[1]);
        ClientResponse<LinkResponse> clientResponse = botService.removeLinkForChat(telegramChatId, request);

        if (clientResponse.error() == null) {
            return new SendMessage(telegramChatId, properties.getProperty("command.untrack.success"));
        }

        ApiErrorResponse error = clientResponse.error();
        return new SendMessage(
                telegramChatId,
                properties
                        .getProperty("command.untrack.fail")
                        .formatted(
                                error.description() == null
                                        ? properties.getProperty("error.internal.command")
                                        : error.description()));
    }

    @Override
    public String name() {
        return properties.getProperty("command.untrack.name");
    }

    @Override
    public String description() {
        return properties.getProperty("command.untrack.description");
    }
}
