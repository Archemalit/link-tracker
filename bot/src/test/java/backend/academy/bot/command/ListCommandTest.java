package backend.academy.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.bot.api.dto.ApiErrorResponse;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.api.dto.LinkResponse;
import backend.academy.bot.api.dto.ListLinksResponse;
import backend.academy.bot.service.bot.BotService;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.List;
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
public class ListCommandTest {
    private static Update update;
    private static Message message;
    private static Chat chat;
    private static User user;

    @Mock
    private BotService botService;

    @Mock
    private Properties properties;

    @InjectMocks
    private ListCommand listCommand;

    private static final Long TELEGRAM_CHAT_ID = 1L;

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
    @DisplayName("Проверка команды /list бота на получение нескольких ссылок")
    public void testLinksTrackList() {
        // GIVEN
        String linkGit1 = "https://github.com/example/test";
        String linkStack1 = "https://stackoverflow.com/questions/1234567/how-to-test";
        String tag = null;

        ListLinksResponse listLinksResponse = new ListLinksResponse(
                List.of(new LinkResponse(TELEGRAM_CHAT_ID, linkGit1), new LinkResponse(TELEGRAM_CHAT_ID, linkStack1)));

        when(message.text()).thenReturn("/list");
        when(botService.getAllLinksForChat(TELEGRAM_CHAT_ID, tag))
                .thenReturn(new ClientResponse<>(listLinksResponse, null));
        when(properties.getProperty("command.list.name")).thenReturn("/list");
        when(properties.getProperty("command.list.links")).thenReturn("Отслеживаемые ссылки:");

        // WHEN
        SendMessage sendMessage = listCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo("Отслеживаемые ссылки:\n" + linkGit1 + "\n" + linkStack1);
    }

    @Test
    @DisplayName("Проверка команды /list бота на ответ о том, что отслеживаемых ссылок нет")
    public void testLinksTrackListEmpty() {
        // GIVEN
        String noLinks = "Вы не отслеживаете никакие ссылки";
        String tag = null;
        ListLinksResponse listLinksResponse = new ListLinksResponse(List.of());

        when(message.text()).thenReturn("/list");
        when(botService.getAllLinksForChat(TELEGRAM_CHAT_ID, tag))
                .thenReturn(new ClientResponse<>(listLinksResponse, null));
        when(properties.getProperty("command.list.name")).thenReturn("/list");
        when(properties.getProperty("command.list.no_links")).thenReturn(noLinks);

        // WHEN
        SendMessage sendMessage = listCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(noLinks);
    }

    @Test
    @DisplayName("Проверка команды /list бота на ответ о том, что нет доступа к ссылкам")
    public void testLinksTrackListFail() {
        // GIVEN
        String noAccess = "Ошибка доступа к ссылкам: %s";
        String errorInternal = "не получилось соединиться с сервером";
        String tag = null;

        when(message.text()).thenReturn("/list");
        when(botService.getAllLinksForChat(TELEGRAM_CHAT_ID, tag))
                .thenReturn(new ClientResponse<>(null, new ApiErrorResponse(null, null, null, null, null)));
        when(properties.getProperty("command.list.name")).thenReturn("/list");
        when(properties.getProperty("command.list.fail.links")).thenReturn(noAccess);
        when(properties.getProperty("error.internal.command")).thenReturn(errorInternal);

        // WHEN
        SendMessage sendMessage = listCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(noAccess.formatted(errorInternal));
    }
}
