package backend.academy.scrapper.handler.scheduler;

import backend.academy.scrapper.client.RestClientGitHub;
import backend.academy.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.scrapper.dto.response.github.GitHubResponse;
import backend.academy.scrapper.exception.github.GitHubApiCallException;
import backend.academy.scrapper.exception.github.GitHubRepositoryNotFound;
import backend.academy.scrapper.model.LinkType;
import backend.academy.scrapper.model.ParsedLink;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import backend.academy.scrapper.repository.jpa.entity.ParseRequestFilter;
import backend.academy.scrapper.service.BotUpdateSender;
import backend.academy.scrapper.util.LinkParser;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Log4j2
public class GitHubUpdateHandler implements LinkUpdateHandler {
    private final RestClientGitHub restClientGitHub;
    private final ParseRequestRepository parseRequestRepository;
    private final LinkRepository linkRepository;
    private final BotUpdateSender botUpdateSender;

    @Override
    public boolean supports(LinkType type) {
        return type == LinkType.GITHUB;
    }

    @Override
    public void handle(Link link, List<ParseRequest> requests) {
        ParsedLink parsed = LinkParser.parseLinkType(link.url());

        try {
            List<GitHubResponse> newIssues =
                    restClientGitHub.getIssueInfo(parsed.owner(), parsed.id(), link.lastUpdate(), null);
            LocalDateTime nowTime = chooseLastDateTimeForGitHub(newIssues, link);

            if (nowTime == null) return;

            link.lastUpdate(nowTime);
            linkRepository.save(link);

            for (ParseRequest parseRequest : requests) {
                List<String> banUsers = parseRequest.filters().stream()
                        .map(ParseRequestFilter::filter)
                        .toList();
                Instant lastView = parseRequest.lastView().toInstant(ZoneOffset.UTC);
                StringBuilder result = new StringBuilder();
                int countNew = 0;

                for (GitHubResponse response : newIssues) {
                    if (response.createdAt().isAfter(lastView) && !banUsers.contains(response.getUserName())) {
                        result.append(response).append("\n\n");
                        countNew++;
                    }
                }

                parseRequest.lastView(nowTime);
                parseRequestRepository.save(parseRequest);

                if (countNew > 0) {
                    botUpdateSender.sendUpdate(
                            new LinkUpdateRequest(parseRequest.chat().telegramChatId(), link.url(), result.toString()),
                            false);
                }
            }

        } catch (GitHubRepositoryNotFound e) {
            for (ParseRequest parseRequest : requests) {
                Long chatId = parseRequest.chat().telegramChatId();
                parseRequestRepository.deleteByChatIdAndLinkId(
                        parseRequest.chat().id(), link.id());
                botUpdateSender.sendUpdate(
                        new LinkUpdateRequest(
                                chatId,
                                link.url(),
                                "Репозиторий GitHub (" + link.url() + ") удалили или сделали приватным"),
                        true);
            }
        } catch (GitHubApiCallException ex) {
            log.error("{} {}", ex.getMessage(), link.url());
        }
    }

    private LocalDateTime chooseLastDateTimeForGitHub(List<GitHubResponse> newIssues, Link link) {
        if (newIssues.isEmpty()) {
            return null;
        }
        LocalDateTime nowTime =
                newIssues.getFirst().createdAt().atOffset(ZoneOffset.UTC).toLocalDateTime();
        if (nowTime.isBefore(link.lastUpdate())) {
            return link.lastUpdate();
        }
        link.lastUpdate(nowTime);
        return nowTime;
    }
}
