package backend.academy.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.bot.api.dto.ApiErrorResponse;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.service.bot.BotService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
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
public class StartCommandTest {
    private static Update update;
    private static Message message;
    private static Chat chat;
    private static User user;
    private static final Long TELEGRAM_CHAT_ID = 1L;

    @Mock
    private BotService botService;

    @Mock
    private Properties properties;

    @InjectMocks
    private StartCommand startCommand;

    @BeforeAll
    public static void initialize() {
        update = mock(Update.class);
        message = mock(Message.class);
        chat = mock(Chat.class);
        user = mock(User.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(message.from()).thenReturn(user);
        when(chat.id()).thenReturn(TELEGRAM_CHAT_ID);
    }

    @Test
    @DisplayName("Проверка команды /start и получение успешного ответа")
    public void testStartCommandSuccess() {
        // GIVEN
        String helloMessage = "Добро пожаловать в бота!";
        when(botService.registerUser(TELEGRAM_CHAT_ID)).thenReturn(new ClientResponse<>(null, null));
        when(properties.getProperty("command.start.register")).thenReturn(helloMessage);

        // WHEN
        SendMessage sendMessage = startCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(helloMessage);
    }

    @Test
    @DisplayName("Проверка команды /start и получение неуспешного ответа")
    public void testStartCommandFail() {
        // GIVEN
        String helloMessage = "Ошибка на этапе регистрации: %s";
        String errorInternal = "не получилось соединиться с сервером";
        when(botService.registerUser(TELEGRAM_CHAT_ID))
                .thenReturn(new ClientResponse<>(null, new ApiErrorResponse(null, null, null, null, null)));
        when(properties.getProperty("command.start.fail.register")).thenReturn(helloMessage);
        when(properties.getProperty("error.internal.command")).thenReturn(errorInternal);

        // WHEN
        SendMessage sendMessage = startCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(helloMessage.formatted(errorInternal));
    }
}
