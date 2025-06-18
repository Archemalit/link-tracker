package backend.academy.bot;

import backend.academy.bot.util.CommandCollector;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkTrackerBot implements Bot {
    private final TelegramBot bot;
    private final UpdatesListener listener;
    private final CommandCollector commandCollector;

    @Override
    @PostConstruct
    public void start() {
        setCommands();
        setListener();
    }

    private void setCommands() {
        Map<String, String> commands = commandCollector.getHandlesNameDescription();
        BotCommand[] menu = new BotCommand[commands.size()];
        int count = 0;
        for (Map.Entry<String, String> command : commands.entrySet()) {
            menu[count++] = new BotCommand(command.getKey(), command.getValue());
        }
        bot.execute(new SetMyCommands(menu));
    }

    private void setListener() {
        bot.setUpdatesListener(listener);
    }

    @Override
    public void close() {
        bot.shutdown();
    }
}
