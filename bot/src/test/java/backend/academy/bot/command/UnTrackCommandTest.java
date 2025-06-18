package backend.academy.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.bot.api.dto.ApiErrorResponse;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.api.dto.LinkResponse;
import backend.academy.bot.api.dto.RemoveLinkRequest;
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
public class UnTrackCommandTest {
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
    private UnTrackCommand unTrackCommand;

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
    @DisplayName("Проверка команды '/untrack <ССЫЛКА>' на ответ о том, что ссылка больше не отслеживается")
    public void testUnTrackLinkSuccess() {
        // GIVEN
        String unTrackSuccess = "Вы успешно перестали отслеживать ссылку";
        String link = "https://github.com/example/test";

        when(botService.removeLinkForChat(TELEGRAM_CHAT_ID, new RemoveLinkRequest(link)))
                .thenReturn(new ClientResponse<>(new LinkResponse(TELEGRAM_CHAT_ID, link), null));
        when(properties.getProperty("command.untrack.success")).thenReturn(unTrackSuccess);
        when(message.text()).thenReturn("/untrack " + link);

        // WHEN
        SendMessage sendMessage = unTrackCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(unTrackSuccess);
    }

    @Test
    @DisplayName(
            "Проверка команды '/untrack <ССЫЛКА> <ССЫЛКА>' на ответ о том, что нельзя передавать сразу несколько ссылок")
    public void testUnTrackLinkManyLinks() {
        // GIVEN
        String manyUrls = "За один раз можно передать только одну ссылку";
        String linkGit1 = "https://github.com/example/test";
        String linkStack1 = "https://stackoverflow.com/questions/1234567/how-to-test";

        when(properties.getProperty("command.untrack.manyurls")).thenReturn(manyUrls);
        when(message.text()).thenReturn("/untrack " + linkGit1 + " " + linkStack1);

        // WHEN
        SendMessage sendMessage = unTrackCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(manyUrls);
    }

    @Test
    @DisplayName("Проверка команды '/untrack' на ответ о том, что нужно передать ссылку")
    public void testUnTrackLinkWithOutLink() {
        // GIVEN
        String noUrl = "Нужно передать ссылку (пример: /untrack <ССЫЛКА>)";

        when(properties.getProperty("command.untrack.nourl")).thenReturn(noUrl);
        when(message.text()).thenReturn("/untrack");

        // WHEN
        SendMessage sendMessage = unTrackCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(noUrl);
    }

    @Test
    @DisplayName("Проверка команды '/untrack <ССЫЛКА>' на ответ о том, что не получилось перестать отслеживать ссылку")
    public void testUnTrackLinkFail() {
        // GIVEN
        String trackFail = "Не получилось перестать отслеживать ссылку: %s";
        String errorInternal = "не получилось соединиться с сервером";
        String link = "https://github.com/example/test";

        when(botService.removeLinkForChat(TELEGRAM_CHAT_ID, new RemoveLinkRequest(link)))
                .thenReturn(new ClientResponse<>(null, new ApiErrorResponse(null, null, null, null, null)));
        when(properties.getProperty("command.untrack.fail")).thenReturn(trackFail);
        when(properties.getProperty("error.internal.command")).thenReturn(errorInternal);
        when(message.text()).thenReturn("/untrack " + link);

        // WHEN
        SendMessage sendMessage = unTrackCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(trackFail.formatted(errorInternal));
    }
}
