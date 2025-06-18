package backend.academy.scrapper.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import backend.academy.scrapper.IntegrationEnvironment;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import backend.academy.scrapper.service.ChatService;
import backend.academy.scrapper.service.LinkService;
import io.micrometer.core.instrument.MeterRegistry;
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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class LinkTypeDataBaseMetricTest extends IntegrationEnvironment {
    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private LinkTypeDataBaseMetric linkTypeDataBaseMetric;

    private static final Long TELEGRAM_CHAT_ID = 1L;

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

    @MockitoSpyBean
    private ParseRequestRepository parseRequestRepository;

    private static Chat chat;
    private static Link link;

    @BeforeEach
    @Transactional
    void setup() {
        meterRegistry.forEachMeter(meterRegistry::remove);

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
    @DisplayName("Проверка работы метрик на количество отслеживаемых ссылок")
    void handle() {
        // WHEN
        linkTypeDataBaseMetric.update();
        // THEN
        assertThat(linkTypeDataBaseMetric.countGitHub.get()).isEqualTo(1L);
    }
}
