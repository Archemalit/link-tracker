package backend.academy.scrapper.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.IntegrationEnvironment;
import backend.academy.scrapper.mapper.ChatMapper;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.mapper.ParseRequestExtractor;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@Import({
    JdbcLinkRepository.class,
    LinkMapper.class,
    JdbcChatRepository.class,
    ChatMapper.class,
    JdbcParseRequestRepository.class,
    ParseRequestExtractor.class
})
@ActiveProfiles("jdbc")
public class JdbcParseRequestRepositoryTest extends IntegrationEnvironment {

    @Autowired
    private ParseRequestRepository jdbcParseRequestRepository;

    @Autowired
    private ChatRepository jdbcChatRepository;

    @Autowired
    private LinkRepository jdbcLinkRepository;

    private Chat chat;
    private Link link;
    private ParseRequest parseRequest;

    @BeforeEach
    void setUp() {
        chat = Chat.builder().telegramChatId(12345L).build();
        link = Link.builder()
                .url("https://stackoverflow.com/questions/123/ex")
                .lastUpdate(LocalDateTime.now())
                .build();

        chat = jdbcChatRepository.save(chat);
        link = jdbcLinkRepository.save(link);

        parseRequest = ParseRequest.builder()
                .chat(chat)
                .link(link)
                .tagName("test")
                .lastView(LocalDateTime.now())
                .build();
        parseRequest = jdbcParseRequestRepository.save(parseRequest);
    }

    @Test
    @DisplayName("Поиск запроса по chat_id и link_id")
    void testFindParseRequestByChatIdAndLinkId() {
        // WHEN
        ParseRequest foundParseRequest = jdbcParseRequestRepository.findFirstByChatIdAndLinkId(chat.id(), link.id());

        // THEN
        assertThat(foundParseRequest).isNotNull();
        assertThat(foundParseRequest.chat().id()).isEqualTo(chat.id());
        assertThat(foundParseRequest.link().id()).isEqualTo(link.id());
    }

    @Test
    @DisplayName("Поиск всех запросов по telegram_chat_id")
    void testFindAllParseRequestsByTelegramChatId() {
        // WHEN
        List<ParseRequest> foundParseRequests =
                jdbcParseRequestRepository.findAllByChatTelegramChatId(chat.telegramChatId());

        // THEN
        assertThat(foundParseRequests).isNotEmpty();
        assertThat(foundParseRequests.getFirst().chat().telegramChatId()).isEqualTo(chat.telegramChatId());
    }

    @Test
    @DisplayName("Получение всех запросов")
    void testFindAllParseRequests() {
        // WHEN
        List<ParseRequest> allParseRequests = jdbcParseRequestRepository.findAll();

        // THEN
        assertThat(allParseRequests).isNotEmpty();
        assertThat(allParseRequests.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Удаление запроса по chat_id и link_url")
    void testDeleteParseRequestByChatIdAndLinkUrl() {
        // WHEN
        int deletedCount = jdbcParseRequestRepository.deleteByChatIdAndLinkUrl(chat.id(), link.url());

        // THEN
        assertThat(deletedCount).isEqualTo(1);
        assertThat(jdbcParseRequestRepository.findFirstByChatIdAndLinkId(chat.id(), link.id()))
                .isNull();
    }

    @Test
    @DisplayName("Удаление запроса по chat_id и link_id")
    void testDeleteParseRequestByChatIdAndLinkId() {
        // WHEN
        jdbcParseRequestRepository.deleteByChatIdAndLinkId(chat.id(), link.id());

        // THEN
        assertThat(jdbcParseRequestRepository.findFirstByChatIdAndLinkId(chat.id(), link.id()))
                .isNull();
    }
}
