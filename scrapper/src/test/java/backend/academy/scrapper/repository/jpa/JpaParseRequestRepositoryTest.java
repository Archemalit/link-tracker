package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.IntegrationEnvironment;
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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("jpa")
public class JpaParseRequestRepositoryTest extends IntegrationEnvironment {

    @Autowired
    private ParseRequestRepository jpaParseRequestRepository;

    @Autowired
    private ChatRepository jpaChatRepository;

    @Autowired
    private LinkRepository jpaLinkRepository;

    private Chat chat;
    private Link link;
    private ParseRequest parseRequest;

    @BeforeEach
    void setUp() {
        chat = Chat.builder().telegramChatId(12345L).build();
        chat = jpaChatRepository.save(chat);

        link = Link.builder()
                .url("https://stackoverflow.com/questions/123/example")
                .lastUpdate(LocalDateTime.now())
                .build();
        link = jpaLinkRepository.save(link);

        parseRequest = ParseRequest.builder()
                .chat(chat)
                .link(link)
                .lastView(LocalDateTime.now())
                .build();
        parseRequest = jpaParseRequestRepository.save(parseRequest);
    }

    @Test
    @DisplayName("Поиск запроса по chat_id и link_id")
    void testFindParseRequestByChatIdAndLinkId() {
        // WHEN
        ParseRequest foundParseRequest = jpaParseRequestRepository.findFirstByChatIdAndLinkId(chat.id(), link.id());

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
                jpaParseRequestRepository.findAllByChatTelegramChatId(chat.telegramChatId());

        // THEN
        assertThat(foundParseRequests).isNotEmpty();
        assertThat(foundParseRequests.getFirst().chat().telegramChatId()).isEqualTo(chat.telegramChatId());
    }

    @Test
    @DisplayName("Получение всех запросов")
    void testFindAllParseRequests() {
        // WHEN
        List<ParseRequest> allParseRequests = jpaParseRequestRepository.findAll();

        // THEN
        assertThat(allParseRequests).isNotEmpty();
        assertThat(allParseRequests.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Удаление запроса по chat_id и link_url")
    void testDeleteParseRequestByChatIdAndLinkUrl() {
        // WHEN
        int deletedCount = jpaParseRequestRepository.deleteByChatIdAndLinkUrl(chat.id(), link.url());

        // THEN
        assertThat(deletedCount).isEqualTo(1);
        assertThat(jpaParseRequestRepository.findFirstByChatIdAndLinkId(chat.id(), link.id()))
                .isNull();
    }

    @Test
    @DisplayName("Удаление запроса по chat_id и link_id")
    void testDeleteParseRequestByChatIdAndLinkId() {
        // WHEN
        jpaParseRequestRepository.deleteByChatIdAndLinkId(chat.id(), link.id());

        // THEN
        assertThat(jpaParseRequestRepository.findFirstByChatIdAndLinkId(chat.id(), link.id()))
                .isNull();
    }
}
