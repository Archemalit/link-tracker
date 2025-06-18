package backend.academy.scrapper.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.RestClientStackOverFlow;
import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.exception.EmptyLinkException;
import backend.academy.scrapper.exception.LinkAlreadyTrackedException;
import backend.academy.scrapper.exception.LinkNotFoundException;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import backend.academy.scrapper.service.impl.LinkServiceImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("jpa")
public class LinkServiceTest {
    @Mock
    private LinkRepository linkRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ParseRequestRepository parseRequestRepository;

    @Mock
    private RestClientStackOverFlow restClientStackOverFlow;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private LinkServiceImpl linkService;

    @Test
    @DisplayName("Добавление новой ссылки в чат, которая до этого никогда не отслеживалась")
    public void testAddLinkForChatNewLink() {
        // GIVEN
        long chatId = 1L;
        long telegramChatId = 12345L;

        Chat chat = Chat.builder().id(chatId).telegramChatId(telegramChatId).build();
        Link link = Link.builder()
                .url("https://stackoverflow.com/questions/123/new")
                .lastUpdate(LocalDateTime.now())
                .build();

        Link linkWithId = Link.builder()
                .id(1L)
                .url("https://stackoverflow.com/questions/123/new")
                .lastUpdate(LocalDateTime.now())
                .build();

        List<String> filters = new ArrayList<>();
        AddLinkRequest addLinkRequest = new AddLinkRequest(link.url(), null, filters);

        when(chatRepository.findFirstByTelegramChatId(telegramChatId)).thenReturn(chat);
        when(linkRepository.findFirstByUrl(link.url())).thenReturn(null);
        when(linkRepository.save(link)).thenReturn(linkWithId);
        when(parseRequestRepository.save(any())).thenReturn(any());
        when(restClientStackOverFlow.getLastAnswerDate(link.url())).thenReturn(12345678L);

        // WHEN
        LinkResponse response = linkService.addLinkForChat(telegramChatId, addLinkRequest);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.url()).isEqualTo(link.url());
    }

    @Test
    @DisplayName("Добавление пустой ссылки в чат и выброс исключения")
    public void testAddLinkForChatNoLink() {
        // GIVEN
        long telegramChatId = 1L;
        List<String> filters = new ArrayList<>();
        AddLinkRequest addLinkRequest = new AddLinkRequest(null, null, filters);

        // WHEN & THEN
        assertThatThrownBy(() -> linkService.addLinkForChat(telegramChatId, addLinkRequest))
                .isInstanceOf(EmptyLinkException.class);

        verify(parseRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Добавление дублирующейся ссылки в чат и выброс исключения")
    public void testAddLinkForChatDuplicateLink() {
        // GIVEN
        long chatId = 1L;
        long telegramChatId = 12345L;

        long linkId = 1L;
        String url = "https://stackoverflow.com/questions/123/new";
        LocalDateTime linkLastUpdate = LocalDateTime.now();

        List<String> filters = new ArrayList<>();

        Chat chat = Chat.builder().id(chatId).telegramChatId(telegramChatId).build();

        Link link =
                Link.builder().id(linkId).url(url).lastUpdate(linkLastUpdate).build();

        ParseRequest parseRequest = ParseRequest.builder()
                .id(1L)
                .chat(chat)
                .link(link)
                .tagName(null)
                .filters(new ArrayList<>())
                .lastView(linkLastUpdate)
                .build();

        AddLinkRequest addLinkRequest = new AddLinkRequest(link.url(), null, filters);

        when(chatRepository.findFirstByTelegramChatId(telegramChatId)).thenReturn(chat);
        when(linkRepository.findFirstByUrl(link.url())).thenReturn(link);
        when(parseRequestRepository.findFirstByChatIdAndLinkId(chatId, linkId)).thenReturn(parseRequest);

        // WHEN & THEN
        assertThatThrownBy(() -> linkService.addLinkForChat(telegramChatId, addLinkRequest))
                .isInstanceOf(LinkAlreadyTrackedException.class);

        verify(parseRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Получение всех ссылок по chatId, когда ссылки существуют")
    public void testGetAllLinksByChatId() {
        // GIVEN
        long telegramChatId = 12345L;

        Chat chat = Chat.builder().id(1L).telegramChatId(telegramChatId).build();

        Link firstLink = Link.builder()
                .id(1L)
                .url("https://stackoverflow.com/questions/123/new")
                .lastUpdate(LocalDateTime.now())
                .build();
        Link secondLink = Link.builder()
                .id(2L)
                .url("https://github.com/someuser/somerepo")
                .lastUpdate(LocalDateTime.now())
                .build();

        ParseRequest firstParseRequest = ParseRequest.builder()
                .id(1L)
                .chat(chat)
                .link(firstLink)
                .tagName(null)
                .filters(new ArrayList<>())
                .lastView(LocalDateTime.now())
                .build();

        ParseRequest secondParseRequest = ParseRequest.builder()
                .id(2L)
                .chat(chat)
                .link(secondLink)
                .tagName(null)
                .filters(new ArrayList<>())
                .lastView(LocalDateTime.now())
                .build();

        when(chatRepository.findFirstByTelegramChatId(telegramChatId)).thenReturn(chat);
        when(parseRequestRepository.findAllByChatTelegramChatId(telegramChatId))
                .thenReturn(List.of(firstParseRequest, secondParseRequest));

        // WHEN
        ListLinksResponse response = linkService.getAllLinksByChatIdAndTag(telegramChatId, null);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.links()).hasSize(2);
        assertThat(response.links().get(0).url()).isEqualTo(firstLink.url());
        assertThat(response.links().get(1).url()).isEqualTo(secondLink.url());
    }

    @Test
    @DisplayName("Удаление ссылки из чата, если ссылка найдена")
    public void testDeleteLinkFromChatValidLink() {
        // GIVEN
        long telegramChatId = 12345L;

        Chat chat = Chat.builder().id(1L).telegramChatId(telegramChatId).build();

        Link link = Link.builder()
                .id(1L)
                .url("https://stackoverflow.com/questions/123/new")
                .lastUpdate(LocalDateTime.now())
                .build();

        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(link.url());

        when(chatRepository.findFirstByTelegramChatId(telegramChatId)).thenReturn(chat);
        when(parseRequestRepository.deleteByChatIdAndLinkUrl(chat.id(), link.url()))
                .thenReturn(1);

        // WHEN
        LinkResponse response = linkService.deleteLinkFromChat(telegramChatId, removeLinkRequest);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.url()).isEqualTo(link.url());
    }

    @Test
    @DisplayName("Удаление ссылки из чата, если ссылка не найдена")
    public void testDeleteLinkFromChatLinkNotFound() {
        // GIVEN
        long telegramChatId = 12345L;

        Chat chat = Chat.builder().id(1L).telegramChatId(telegramChatId).build();

        Link link = Link.builder()
                .id(1L)
                .url("https://stackoverflow.com/questions/123/new")
                .lastUpdate(LocalDateTime.now())
                .build();

        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest(link.url());

        when(chatRepository.findFirstByTelegramChatId(telegramChatId)).thenReturn(chat);
        when(parseRequestRepository.deleteByChatIdAndLinkUrl(chat.id(), link.url()))
                .thenReturn(0);

        // WHEN & THEN
        assertThatThrownBy(() -> linkService.deleteLinkFromChat(telegramChatId, removeLinkRequest))
                .isInstanceOf(LinkNotFoundException.class);
    }
}
