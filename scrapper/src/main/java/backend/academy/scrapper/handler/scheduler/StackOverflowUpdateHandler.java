package backend.academy.scrapper.handler.scheduler;

import backend.academy.scrapper.client.RestClientStackOverFlow;
import backend.academy.scrapper.dto.request.LinkUpdateRequest;
import backend.academy.scrapper.dto.response.stackoverflow.Answer;
import backend.academy.scrapper.exception.stackoverflow.StackOverFlowNotFound;
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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StackOverflowUpdateHandler implements LinkUpdateHandler {
    private final RestClientStackOverFlow restClientStackOverflow;
    private final ParseRequestRepository parseRequestRepository;
    private final LinkRepository linkRepository;
    private final BotUpdateSender botUpdateSender;

    @Override
    public boolean supports(LinkType type) {
        return type == LinkType.STACK_OVER_FLOW;
    }

    @Override
    public void handle(Link link, List<ParseRequest> requests) {
        ParsedLink parsed = LinkParser.parseLinkType(link.url());

        try {
            // TODO: подумать с самим вопросом
            // StackOverflowQuestion question = restClientStackOverflow.getQuestionInformation(parsed.id());
            List<Answer> newAnswers = restClientStackOverflow.getNewAnswers(parsed.id(), link.lastUpdate());
            LocalDateTime nowTime = chooseLastDateTimeForStackOverFlow(newAnswers, link);

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

                for (Answer answer : newAnswers) {
                    Instant creation = Instant.ofEpochSecond(answer.creationDate());
                    if (creation.isAfter(lastView) && !banUsers.contains(answer.getOwnerDisplayName())) {
                        result.append(answer).append("\n");
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

        } catch (StackOverFlowNotFound e) {
            for (ParseRequest parseRequest : requests) {
                Long chatId = parseRequest.chat().telegramChatId();
                parseRequestRepository.deleteByChatIdAndLinkId(
                        parseRequest.chat().id(), link.id());
                botUpdateSender.sendUpdate(
                        new LinkUpdateRequest(
                                chatId, link.url(), "Вопрос StackOverflow (" + link.url() + ") был удалён или скрыт"),
                        true);
            }
        }
    }

    private LocalDateTime chooseLastDateTimeForStackOverFlow(List<Answer> newAnswerResponses, Link link) {
        if (newAnswerResponses.isEmpty()) {
            return null;
        }

        LocalDateTime nowTime = Instant.ofEpochSecond(
                        newAnswerResponses.getFirst().creationDate())
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
        if (nowTime.isBefore(link.lastUpdate())) {
            return link.lastUpdate();
        }
        link.lastUpdate(nowTime);
        return nowTime;
    }
}
