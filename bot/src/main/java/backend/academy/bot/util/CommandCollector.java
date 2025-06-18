package backend.academy.bot.util;

import backend.academy.bot.command.CommandHandler;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@ComponentScan(basePackages = "backend.academy.bot.command")
public class CommandCollector {
    private final Map<String, CommandHandler> nameHandlers = new HashMap<>();
    private final Map<String, String> handlesNameDescription = new HashMap<>();

    public CommandCollector(List<CommandHandler> handlers) {
        for (CommandHandler handler : handlers) {
            handlesNameDescription.put(handler.name(), handler.description());
            nameHandlers.put(handler.name(), handler);
        }
    }

    public Map<String, String> getHandlesNameDescription() {
        return handlesNameDescription;
    }

    public Map<String, CommandHandler> getNameHandlers() {
        return nameHandlers;
    }
}
