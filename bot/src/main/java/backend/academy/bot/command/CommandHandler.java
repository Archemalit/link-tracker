package backend.academy.bot.command;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.Properties;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class CommandHandler {
    protected final Properties properties;

    public boolean supports(Update update) {
        return update.message() != null && update.message().text() != null;
    }

    public abstract SendMessage handle(Update update);

    public abstract String name();

    public abstract String description();
}
