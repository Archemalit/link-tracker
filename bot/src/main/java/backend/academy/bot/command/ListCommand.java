package backend.academy.bot.command;

import backend.academy.bot.api.dto.ApiErrorResponse;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.api.dto.LinkResponse;
import backend.academy.bot.api.dto.ListLinksResponse;
import backend.academy.bot.service.bot.BotService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Properties;
import org.springframework.stereotype.Component;

@Component
public class ListCommand extends CommandHandler {
    private final BotService botService;

    public ListCommand(Properties properties, BotService botService) {
        super(properties);
        this.botService = botService;
    }

    @Override
    public SendMessage handle(Update update) {
        long telegramChatId = update.message().chat().id();
        String tag = update.message().text().replace(name(), "").trim();
        if (tag.isEmpty()) tag = null;

        ClientResponse<ListLinksResponse> clientResponse = botService.getAllLinksForChat(telegramChatId, tag);
        if (clientResponse.error() == null) {
            StringBuilder builder = new StringBuilder();
            if (clientResponse.response().size() != 0) {
                builder.append(properties.getProperty("command.list.links"));

                for (LinkResponse linksResponse : clientResponse.response().links()) {
                    builder.append("\n").append(linksResponse.url());
                }
            } else {
                if (tag != null) {
                    builder.append(
                            properties.getProperty("command.list.no_tag_links").formatted(tag));
                } else {
                    builder.append(properties.getProperty("command.list.no_links"));
                }
            }

            return new SendMessage(telegramChatId, builder.toString());
        }

        ApiErrorResponse error = clientResponse.error();
        return new SendMessage(
                telegramChatId,
                properties
                        .getProperty("command.list.fail.links")
                        .formatted(
                                error.description() == null
                                        ? properties.getProperty("error.internal.command")
                                        : error.description()));
    }

    @Override
    public String name() {
        return properties.getProperty("command.list.name");
    }

    @Override
    public String description() {
        return properties.getProperty("command.list.description");
    }
}
