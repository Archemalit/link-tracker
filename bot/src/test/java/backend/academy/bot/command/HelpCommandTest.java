package backend.academy.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.bot.util.CommandCollector;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// TODO: переписать эти тесты по примеру NotificationCommandTest с SpringBootTest
@ExtendWith(MockitoExtension.class)
public class HelpCommandTest {
    private static Update update;
    private static Message message;
    private static Chat chat;

    @Mock
    private Properties properties;

    @Mock
    private CommandCollector commandCollector;

    @InjectMocks
    private HelpCommand helpCommand;

    @BeforeAll
    public static void initialize() {
        update = mock(Update.class);
        message = mock(Message.class);
        chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(1L);
    }

    @Test
    @DisplayName("Проверка на доступные команды бота")
    public void testHelpMessage() {
        // GIVEN
        Map<String, String> nameAndDescription = new HashMap<>();
        nameAndDescription.put("/start", "Начать работу с ботом");
        nameAndDescription.put("/help", "Помощь в командах");
        nameAndDescription.put("/list", "Получить все отслеживаемые ссылки");
        nameAndDescription.put("/track", "Начать отслеживать ссылку");
        nameAndDescription.put("/untrack", "Перестать отслеживать ссылку");

        when(properties.getProperty("command.help.list")).thenReturn("Доступные команды:");
        when(commandCollector.getHandlesNameDescription()).thenReturn(nameAndDescription);

        // WHEN
        SendMessage sendMessage = helpCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text"))
                .isEqualTo(
                        """
                        Доступные команды:
                        /help - Помощь в командах
                        /list - Получить все отслеживаемые ссылки
                        /start - Начать работу с ботом
                        /track - Начать отслеживать ссылку
                        /untrack - Перестать отслеживать ссылку""");
    }
}
