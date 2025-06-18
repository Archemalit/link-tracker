package backend.academy.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.bot.api.dto.AddNotificationTimeRequest;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.config.PropertiesConfig;
import backend.academy.bot.service.bot.BotService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import java.time.LocalTime;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = {PropertiesConfig.class})
@ExtendWith(SpringExtension.class)
public class NotificationCommandTest {
    private static Update update;
    private static Message message;
    private static Chat chat;
    private static User user;
    private static final Long TELEGRAM_CHAT_ID = 1L;

    @Autowired
    private Properties properties;

    @MockitoBean
    private BotService botService;

    private NotificationCommand notificationCommand;

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

    @BeforeEach
    public void setUp() {
        notificationCommand = new NotificationCommand(properties, botService);
    }

    @Test
    @DisplayName(
            "Проверка команды /set_time без параметров на ответ о том, что изменился режим отправки обновлений на сразу")
    public void testSetNotificationTypeImmediate() {
        // GIVEN
        LocalTime notificationTime = null;
        when(botService.addNotificationType(TELEGRAM_CHAT_ID, new AddNotificationTimeRequest(notificationTime)))
                .thenReturn(new ClientResponse<>(null, null));
        when(message.text()).thenReturn("/set_time");

        // WHEN
        SendMessage sendMessage = notificationCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(properties.getProperty("command.notification.immediate"));
    }

    @Test
    @DisplayName(
            "Проверка команды /set_time со временем на ответ о том, что изменился режим отправки обновлений на это время")
    public void testSetNotificationTypeDigest() {
        // GIVEN
        String time = "09:30";
        LocalTime notificationTime = LocalTime.parse(time);
        when(botService.addNotificationType(TELEGRAM_CHAT_ID, new AddNotificationTimeRequest(notificationTime)))
                .thenReturn(new ClientResponse<>(null, null));
        when(message.text()).thenReturn("/set_time " + time);

        // WHEN
        SendMessage sendMessage = notificationCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text"))
                .isEqualTo(properties.getProperty("command.notification.digest").formatted(notificationTime));
    }

    @Test
    @DisplayName(
            "Проверка команды /set_time с некорректным форматом времени на ответ о том, что формат сообщения неправильный")
    public void testSetNotificationTypeWrongFormat() {
        // GIVEN
        String time = "10-30";
        LocalTime notificationTime = null;
        when(botService.addNotificationType(TELEGRAM_CHAT_ID, new AddNotificationTimeRequest(notificationTime)))
                .thenReturn(new ClientResponse<>(null, null));
        when(message.text()).thenReturn("/set_time " + time);

        // WHEN
        SendMessage sendMessage = notificationCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(properties.getProperty("command.notification.wrong.format"));
    }

    @Test
    @DisplayName(
            "Проверка команды /set_time с некорректным форматом времени несколькими словами на ответ о том, что формат сообщения неправильный")
    public void testSetNotificationTypeWrongFormatManyValues() {
        // GIVEN
        String time = "9 hours 30 minutes";
        LocalTime notificationTime = null;
        when(botService.addNotificationType(TELEGRAM_CHAT_ID, new AddNotificationTimeRequest(notificationTime)))
                .thenReturn(new ClientResponse<>(null, null));
        when(message.text()).thenReturn("/set_time " + time);

        // WHEN
        SendMessage sendMessage = notificationCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(properties.getProperty("command.notification.manytimes"));
    }
}
