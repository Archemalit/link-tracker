package backend.academy.scrapper.redis;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.IntegrationEnvironment;
import backend.academy.scrapper.client.RestClientGitHub;
import backend.academy.scrapper.config.RedisConfig;
import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import backend.academy.scrapper.service.ChatService;
import backend.academy.scrapper.service.LinkService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(value = {RedisConfig.class})
@ActiveProfiles("jpa")
@TestPropertySource(
        properties = {
            "app.message-transport=http",
        })
public class RedisLinkServiceCacheTest extends IntegrationEnvironment {
    private static final Long TELEGRAM_CHAT_ID = 123L;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ChatService chatService;

    @Autowired
    private LinkService linkService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private LinkRepository linkRepository;

    @MockitoBean
    private RestClientGitHub restClientGitHub;

    @MockitoSpyBean
    private ParseRequestRepository parseRequestRepository;

    private static Chat chat;
    private static Link link;

    @BeforeEach
    @Transactional
    void setup() {
        chat = Chat.builder().telegramChatId(TELEGRAM_CHAT_ID).build();
        chat = chatRepository.save(chat);

        LocalDateTime updateTime = LocalDateTime.now();
        link = Link.builder()
                .url("https://github.com/test/repo")
                .lastUpdate(updateTime)
                .build();
        link = linkRepository.save(link);
        ParseRequest parseRequest = ParseRequest.builder()
                .chat(chat)
                .link(link)
                .lastView(updateTime)
                .tagName(null)
                .filters(List.of())
                .build();
        parseRequestRepository.save(parseRequest);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        parseRequestRepository.deleteByChatIdAndLinkUrl(TELEGRAM_CHAT_ID, link.url());
        linkRepository.deleteByUrl(link.url());
        chatRepository.deleteByTelegramChatId(TELEGRAM_CHAT_ID);

        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            cache.clear();
        });
    }

    @Test
    @DisplayName("Кеширование: метод получения ссылок по чату должен обращаться в БД только один раз")
    public void shouldCacheGetAllLinks() {
        // WHEN
        linkService.getAllLinksByChatIdAndTag(TELEGRAM_CHAT_ID, null);
        linkService.getAllLinksByChatIdAndTag(TELEGRAM_CHAT_ID, null);

        // THEN
        verify(parseRequestRepository, times(1)).findAllByChatTelegramChatId(TELEGRAM_CHAT_ID);
    }

    @Test
    @DisplayName(
            "Кеширование: метод получения ссылок по чату должен обращаться в БД два раз, потому что мы ещё добавляем в БД новую ссылку")
    public void shouldNotCacheGetAllLinksBecauseOfTrackNewUrl() {
        // GIVEN
        AddLinkRequest addLinkRequest = new AddLinkRequest("https://github.com/test/repo2", null, List.of());
        when(restClientGitHub.getLastAction("test", "repo2")).thenReturn(LocalDateTime.now());

        // WHEN
        linkService.getAllLinksByChatIdAndTag(TELEGRAM_CHAT_ID, null);
        linkService.addLinkForChat(TELEGRAM_CHAT_ID, addLinkRequest);

        linkService.getAllLinksByChatIdAndTag(TELEGRAM_CHAT_ID, null);

        // THEN
        verify(parseRequestRepository, times(2)).findAllByChatTelegramChatId(TELEGRAM_CHAT_ID);
    }

    @Test
    @DisplayName(
            "Кеширование: метод получения ссылок по чату должен обращаться в БД два раз, потому что мы ещё удаляем из БД старую ссылку")
    public void shouldNotCacheGetAllLinksBecauseOfUnTrackUrl() {
        // GIVEN
        RemoveLinkRequest removeLinkRequest = new RemoveLinkRequest("https://github.com/test/repo");

        // WHEN
        linkService.getAllLinksByChatIdAndTag(TELEGRAM_CHAT_ID, null);
        linkService.deleteLinkFromChat(TELEGRAM_CHAT_ID, removeLinkRequest);
        linkService.getAllLinksByChatIdAndTag(TELEGRAM_CHAT_ID, null);

        // THEN
        verify(parseRequestRepository, times(2)).findAllByChatTelegramChatId(TELEGRAM_CHAT_ID);
    }
}
