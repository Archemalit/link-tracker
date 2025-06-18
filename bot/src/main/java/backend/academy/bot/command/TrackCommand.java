package backend.academy.bot.command;

import backend.academy.bot.api.dto.ApiErrorResponse;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.api.dto.LinkResponse;
import backend.academy.bot.repository.link.LinkRepository;
import backend.academy.bot.repository.state.State;
import backend.academy.bot.repository.state.StateRepository;
import backend.academy.bot.service.bot.BotService;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.springframework.stereotype.Component;

@Component
public class TrackCommand extends CommandHandler {
    private final BotService botService;
    private final LinkRepository linkRepository;
    private final StateRepository stateRepository;

    public TrackCommand(
            Properties properties,
            BotService botService,
            LinkRepository linkRepository,
            StateRepository stateRepository) {
        super(properties);
        this.botService = botService;
        this.linkRepository = linkRepository;
        this.stateRepository = stateRepository;
    }

    @Override
    public SendMessage handle(Update update) {
        long telegramChatId = update.message().chat().id();

        if (!stateRepository.contains(telegramChatId)) {
            stateRepository.setState(telegramChatId, State.AWAITING_LINK);
        }

        switch (stateRepository.getState(telegramChatId)) {
            case AWAITING_LINK -> {
                String[] data = update.message().text().trim().split("\\s+");
                if (data.length < 2) {
                    stateRepository.remove(telegramChatId);
                    return new SendMessage(telegramChatId, properties.getProperty("command.track.nourl"));
                }
                if (data.length > 2) {
                    stateRepository.remove(telegramChatId);
                    return new SendMessage(telegramChatId, properties.getProperty("command.track.manyurls"));
                }
                linkRepository.addLink(telegramChatId, data[1]);
                stateRepository.setState(telegramChatId, State.AWAITING_TAGS);
                return new SendMessage(telegramChatId, properties.getProperty("command.track.tags"));
            }
            case AWAITING_TAGS -> {
                String text = update.message().text().trim();

                if (text.equals("-")) {
                    linkRepository.addTag(telegramChatId, null);
                } else {
                    linkRepository.addTag(telegramChatId, text);
                }
                stateRepository.setState(telegramChatId, State.AWAITING_FILTERS);

                return new SendMessage(telegramChatId, properties.getProperty("command.track.filters"));
            }
            case AWAITING_FILTERS -> {
                String text = update.message().text().trim();
                List<String> filters = new ArrayList<>();
                if (text.equals("-")) {
                    linkRepository.addFilters(telegramChatId, filters);
                } else {
                    filters = Arrays.stream(text.split(",")).toList();
                    linkRepository.addFilters(telegramChatId, filters);
                }
                stateRepository.remove(telegramChatId);

                ClientResponse<LinkResponse> clientResponse =
                        botService.addLinkForChat(telegramChatId, linkRepository.getLink(telegramChatId));
                linkRepository.remove(telegramChatId);

                if (clientResponse.error() == null) {
                    return new SendMessage(telegramChatId, properties.getProperty("command.track.success"));
                }

                ApiErrorResponse error = clientResponse.error();
                return new SendMessage(
                        telegramChatId,
                        properties
                                .getProperty("command.track.fail")
                                .formatted(
                                        error.description() == null
                                                ? properties.getProperty("error.internal.command")
                                                : error.description()));
            }
            default -> {
                return new SendMessage(telegramChatId, properties.getProperty("command.unknown.text"));
            }
        }
    }

    @Override
    public String name() {
        return properties.getProperty("command.track.name");
    }

    @Override
    public String description() {
        return properties.getProperty("command.track.description");
    }
}
