package backend.academy.bot.command;

import backend.academy.bot.util.CommandCollector;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Map;
import java.util.Properties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class HelpCommand extends CommandHandler {
    private final CommandCollector commandCollector;

    public HelpCommand(Properties properties, @Lazy CommandCollector commandCollector) {
        super(properties);
        this.commandCollector = commandCollector;
    }

    @Override
    public SendMessage handle(Update update) {
        long telegramChatId = update.message().chat().id();
        Map<String, String> commands = commandCollector.getHandlesNameDescription();
        StringBuilder response = new StringBuilder();
        response.append(properties.getProperty("command.help.list"));

        commands.keySet().stream().sorted().forEach(key -> {
            response.append("\n").append(key).append(" - ").append(commands.get(key));
        });

        return new SendMessage(telegramChatId, response.toString());
    }

    @Override
    public String name() {
        return properties.getProperty("command.help.name");
    }

    @Override
    public String description() {
        return properties.getProperty("command.help.description");
    }
}
