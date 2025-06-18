package backend.academy.bot.command;

import backend.academy.bot.api.dto.AddNotificationTimeRequest;
import backend.academy.bot.api.dto.ApiErrorResponse;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.service.bot.BotService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Properties;
import org.springframework.stereotype.Component;

@Component
public class NotificationCommand extends CommandHandler {
    private final BotService botService;

    public NotificationCommand(Properties properties, BotService botService) {
        super(properties);
        this.botService = botService;
    }

    @Override
    public SendMessage handle(Update update) {
        long telegramChatId = update.message().chat().id();
        String[] data = update.message().text().trim().split("\\s+");

        if (data.length > 2) {
            return new SendMessage(telegramChatId, properties.getProperty("command.notification.manytimes"));
        }

        LocalTime notificationTime = null;
        if (data.length > 1) {
            try {
                notificationTime = LocalTime.parse(data[1]).truncatedTo(ChronoUnit.MINUTES);
            } catch (DateTimeParseException e) {
                return new SendMessage(telegramChatId, properties.getProperty("command.notification.wrong.format"));
            }
        }

        ClientResponse<Void> clientResponse =
                botService.addNotificationType(telegramChatId, new AddNotificationTimeRequest(notificationTime));

        if (Objects.isNull(clientResponse.error())) {
            if (data.length > 1) {
                return new SendMessage(
                        telegramChatId,
                        properties.getProperty("command.notification.digest").formatted(notificationTime));
            }
            return new SendMessage(telegramChatId, properties.getProperty("command.notification.immediate"));
        }

        ApiErrorResponse error = clientResponse.error();
        return new SendMessage(
                telegramChatId,
                properties
                        .getProperty("command.notification.fail")
                        .formatted(
                                error.description() == null
                                        ? properties.getProperty("error.internal.command")
                                        : error.description()));
    }

    @Override
    public String name() {
        return properties.getProperty("command.notification.name");
    }

    @Override
    public String description() {
        return properties.getProperty("command.notification.description");
    }
}
