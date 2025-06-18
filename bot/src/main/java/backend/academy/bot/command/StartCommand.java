package backend.academy.bot.command;

import backend.academy.bot.api.dto.ApiErrorResponse;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.service.bot.BotService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Properties;
import org.springframework.stereotype.Component;

@Component
public class StartCommand extends CommandHandler {
    private final BotService botService;

    public StartCommand(Properties properties, BotService botService) {
        super(properties);
        this.botService = botService;
    }

    @Override
    public SendMessage handle(Update update) {
        long telegramChatId = update.message().chat().id();
        ClientResponse<Void> clientResponse = botService.registerUser(telegramChatId);

        if (clientResponse.error() == null) {
            return new SendMessage(telegramChatId, properties.getProperty("command.start.register"));
        }

        ApiErrorResponse error = clientResponse.error();
        return new SendMessage(
                telegramChatId,
                properties
                        .getProperty("command.start.fail.register")
                        .formatted(
                                error.description() == null
                                        ? properties.getProperty("error.internal.command")
                                        : error.description()));
    }

    @Override
    public String name() {
        return properties.getProperty("command.start.name");
    }

    @Override
    public String description() {
        return properties.getProperty("command.start.description");
    }
}
