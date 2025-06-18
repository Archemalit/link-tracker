package backend.academy.scrapper.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.RestClientStackOverFlow;
import backend.academy.scrapper.dto.response.stackoverflow.Answer;
import backend.academy.scrapper.dto.response.stackoverflow.Owner;
import backend.academy.scrapper.dto.response.stackoverflow.StackOverflowQuestion;
import backend.academy.scrapper.handler.scheduler.StackOverflowUpdateHandler;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import backend.academy.scrapper.service.BotUpdateSender;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StackOverflowUpdateHandlerTest {

    @Mock
    private RestClientStackOverFlow restClientStackOverFlow;

    @Mock
    private ParseRequestRepository parseRequestRepository;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private BotUpdateSender botUpdateSender;

    @InjectMocks
    private StackOverflowUpdateHandler handler;

    @Test
    void handle_ShouldUpdateLinkAndNotifyUser_WhenNewAnswerExists() {
        // GIVEN
        long telegramChatId = 12345L;
        LocalDateTime newLastAnswerDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime lastAnswerDateTime = newLastAnswerDateTime.minusDays(1);

        Chat chat = Chat.builder().id(1L).telegramChatId(telegramChatId).build();

        Link link = Link.builder()
                .id(1L)
                .url("https://stackoverflow.com/questions/123456")
                .lastUpdate(lastAnswerDateTime)
                .build();

        ParseRequest parseRequest = ParseRequest.builder()
                .id(1L)
                .chat(chat)
                .link(link)
                .tagName(null)
                .filters(new ArrayList<>())
                .lastView(lastAnswerDateTime)
                .build();

        StackOverflowQuestion question = StackOverflowQuestion.builder()
                .title("Вопрооос?")
                .body("Содержание вопроса")
                .build();

        Owner owner = Owner.builder().displayName("User_1234").build();

        List<Answer> answers = List.of(
                Answer.builder()
                        .owner(owner)
                        .body("First new answer!")
                        .creationDate(newLastAnswerDateTime.toEpochSecond(ZoneOffset.UTC))
                        .build(),
                Answer.builder()
                        .owner(owner)
                        .body("Second new answer!")
                        .creationDate(newLastAnswerDateTime.toEpochSecond(ZoneOffset.UTC))
                        .build());

        //        when(restClientStackOverFlow.getQuestionInformation("123456")).thenReturn(question);
        when(restClientStackOverFlow.getNewAnswers("123456", lastAnswerDateTime))
                .thenReturn(answers);

        // WHEN
        handler.handle(link, List.of(parseRequest));

        // THEN
        verify(linkRepository).save(any(Link.class));
        verify(parseRequestRepository).save(any(ParseRequest.class));
        assertThat(parseRequest.lastView()).isEqualTo(newLastAnswerDateTime);
    }
}
