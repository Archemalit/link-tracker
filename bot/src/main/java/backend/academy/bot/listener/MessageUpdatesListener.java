package backend.academy.bot.listener;

import backend.academy.bot.command.CommandHandler;
import backend.academy.bot.repository.state.StateRepository;
import backend.academy.bot.sender.ChatSender;
import backend.academy.bot.util.CommandCollector;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
import java.util.Properties;
import org.springframework.stereotype.Component;

@Component
public class MessageUpdatesListener implements UpdatesListener {
    private final ChatSender chatSender;
    private final CommandCollector commandCollector;
    private final StateRepository stateRepository;
    private final Properties properties;

    public MessageUpdatesListener(
            ChatSender chatSender,
            CommandCollector commandCollector,
            StateRepository stateRepository,
            Properties properties) {
        this.chatSender = chatSender;
        this.commandCollector = commandCollector;
        this.stateRepository = stateRepository;
        this.properties = properties;
    }

    @Override
    public int process(List<Update> list) {
        list.forEach(update -> {
            SendMessage message = handle(update);
            if (message != null) {
                chatSender.sendMessage(message);
            }
        });
        return CONFIRMED_UPDATES_ALL;
    }

    private SendMessage handle(Update update) {
        String command = getCommandFromUpdate(update);
        String unknown = properties.getProperty("command.unknown.text");
        CommandHandler handler = commandCollector.getNameHandlers().get(command);

        if (update.message() == null) {
            return null;
        }

        if (stateRepository.contains(update.message().chat().id())) {
            handler = commandCollector.getNameHandlers().get(properties.getProperty("command.track.name"));
        }

        if (handler == null) {
            return new SendMessage(update.message().chat().id(), unknown);
        }

        return handler.handle(update);
    }

    private String getCommandFromUpdate(Update update) {
        if (update.message() != null && update.message().text() != null) {
            return update.message().text().trim().split("\\s+")[0];
        }

        return null;
    }
}
