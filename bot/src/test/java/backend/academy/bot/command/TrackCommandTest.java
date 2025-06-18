package backend.academy.bot.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.bot.api.dto.AddLinkRequest;
import backend.academy.bot.api.dto.ClientResponse;
import backend.academy.bot.api.dto.LinkResponse;
import backend.academy.bot.repository.link.LinkRepository;
import backend.academy.bot.repository.state.State;
import backend.academy.bot.repository.state.StateRepository;
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
public class TrackCommandTest {
    private static Update update;
    private static Message message;
    private static Chat chat;
    private static User user;
    private static final Long TELEGRAM_CHAT_ID = 1L;

    @Mock
    private BotService botService;

    @Mock
    private Properties properties;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private LinkRepository linkRepository;

    @InjectMocks
    private TrackCommand trackCommand;

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
    @DisplayName("Проверка команды '/track <ССЫЛКА>' на ответ о том, что дальше нужно ввести тэги")
    public void testTrackLinkSuccess() {
        // GIVEN
        String link = "https://github.com/example/test";
        String tags = "Теперь введите тэг (если он вам не нужен, то просто отправьте \"-\")";
        when(stateRepository.contains(TELEGRAM_CHAT_ID)).thenReturn(true);
        when(stateRepository.getState(TELEGRAM_CHAT_ID)).thenReturn(State.AWAITING_LINK);
        doNothing().when(stateRepository).setState(eq(TELEGRAM_CHAT_ID), any());
        doNothing().when(linkRepository).addLink(TELEGRAM_CHAT_ID, link);
        when(message.text()).thenReturn("/track " + link);
        when(properties.getProperty("command.track.tags")).thenReturn(tags);

        // WHEN
        SendMessage sendMessage = trackCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(tags);
    }

    @Test
    @DisplayName("Проверка команды '/track <ССЫЛКА>' на ответ о том, что дальше нужно ввести фильтры")
    public void testTrackLinkTagsSuccess() {
        // GIVEN
        String filters = "Теперь введите фильтры через запятую (если они вам не нужны, то просто отправьте \"-\")";
        when(stateRepository.contains(TELEGRAM_CHAT_ID)).thenReturn(true);
        when(stateRepository.getState(TELEGRAM_CHAT_ID)).thenReturn(State.AWAITING_TAGS);
        doNothing().when(stateRepository).setState(eq(TELEGRAM_CHAT_ID), any());
        doNothing().when(linkRepository).addTag(TELEGRAM_CHAT_ID, "car");
        when(message.text()).thenReturn("car");
        when(properties.getProperty("command.track.filters")).thenReturn(filters);

        // WHEN
        SendMessage sendMessage = trackCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(filters);
    }

    @Test
    @DisplayName("Проверка команды '/track <ССЫЛКА>' на ответ о том, что ссылка начала отлеживаться")
    public void testTrackLinkFiltersSuccess() {
        // GIVEN
        String link = "https://github.com/example/test";
        String tag = "car";
        List<String> filterValues = List.of("user1", "user2");
        String done = "Вы успешно начали отслеживать ссылку!";

        AddLinkRequest addLinkRequest = new AddLinkRequest(link, tag, filterValues);

        when(stateRepository.getState(TELEGRAM_CHAT_ID)).thenReturn(State.AWAITING_FILTERS);
        when(stateRepository.contains(TELEGRAM_CHAT_ID)).thenReturn(true);
        doNothing().when(linkRepository).addFilters(TELEGRAM_CHAT_ID, filterValues);
        doNothing().when(linkRepository).remove(TELEGRAM_CHAT_ID);
        doNothing().when(stateRepository).remove(TELEGRAM_CHAT_ID);
        when(message.text()).thenReturn("user1,user2");
        when(properties.getProperty("command.track.success")).thenReturn(done);
        when(linkRepository.getLink(TELEGRAM_CHAT_ID)).thenReturn(addLinkRequest);
        when(botService.addLinkForChat(TELEGRAM_CHAT_ID, addLinkRequest))
                .thenReturn(new ClientResponse<>(new LinkResponse(TELEGRAM_CHAT_ID, link), null));

        // WHEN
        SendMessage sendMessage = trackCommand.handle(update);
        Map<String, Object> parameters = sendMessage.getParameters();

        // THEN
        assertThat(parameters.get("text")).isEqualTo(done);
    }
}
