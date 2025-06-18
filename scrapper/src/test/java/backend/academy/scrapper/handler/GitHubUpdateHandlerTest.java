package backend.academy.scrapper.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import backend.academy.scrapper.client.RestClientGitHub;
import backend.academy.scrapper.dto.response.github.GitHubResponse;
import backend.academy.scrapper.handler.scheduler.GitHubUpdateHandler;
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
class GitHubUpdateHandlerTest {
    @Mock
    private RestClientGitHub restClientGitHub;

    @Mock
    private ParseRequestRepository parseRequestRepository;

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private BotUpdateSender botUpdateSender;

    @InjectMocks
    private GitHubUpdateHandler handler;

    @Test
    void handle_ShouldUpdateLinkAndNotifyUser_WhenNewCommitExists() {
        // GIVEN
        long telegramChatId = 12345L;
        LocalDateTime newLastAnswerDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime lastUpdateTime = newLastAnswerDateTime.minusDays(1);

        Chat chat = Chat.builder().id(1L).telegramChatId(telegramChatId).build();

        Link link = Link.builder()
                .id(1L)
                .url("https://github.com/user/repo")
                .lastUpdate(lastUpdateTime)
                .build();

        ParseRequest parseRequest = ParseRequest.builder()
                .id(1L)
                .chat(chat)
                .link(link)
                .filters(new ArrayList<>())
                .lastView(lastUpdateTime)
                .build();

        GitHubResponse repoInfo = GitHubResponse.builder()
                .title("Что-то тут")
                .body("Тело")
                .createdAt(newLastAnswerDateTime.toInstant(ZoneOffset.UTC))
                .build();

        when(restClientGitHub.getIssueInfo("user", "repo", parseRequest.lastView(), null))
                .thenReturn(List.of(repoInfo));

        // WHEN
        handler.handle(link, List.of(parseRequest));

        // THEN
        verify(linkRepository).save(any(Link.class));
        verify(parseRequestRepository).save(any(ParseRequest.class));
        assertThat(parseRequest.lastView()).isEqualTo(newLastAnswerDateTime);
    }
}
