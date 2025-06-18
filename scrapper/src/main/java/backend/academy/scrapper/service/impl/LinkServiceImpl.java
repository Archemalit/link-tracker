package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.client.RestClientGitHub;
import backend.academy.scrapper.client.RestClientStackOverFlow;
import backend.academy.scrapper.dto.request.AddLinkRequest;
import backend.academy.scrapper.dto.request.RemoveLinkRequest;
import backend.academy.scrapper.dto.response.LinkResponse;
import backend.academy.scrapper.dto.response.ListLinksResponse;
import backend.academy.scrapper.exception.ChatNotFoundException;
import backend.academy.scrapper.exception.EmptyLinkException;
import backend.academy.scrapper.exception.LinkAlreadyTrackedException;
import backend.academy.scrapper.exception.LinkNotFoundException;
import backend.academy.scrapper.exception.UnknownLinkTypeException;
import backend.academy.scrapper.model.LinkType;
import backend.academy.scrapper.model.ParsedLink;
import backend.academy.scrapper.repository.ChatRepository;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.Chat;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import backend.academy.scrapper.repository.jpa.entity.ParseRequestFilter;
import backend.academy.scrapper.service.LinkService;
import backend.academy.scrapper.util.LinkParser;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class LinkServiceImpl implements LinkService {
    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;
    private final ParseRequestRepository parseRequestRepository;
    private final RestClientStackOverFlow restClientStackOverFlow;
    private final RestClientGitHub restClientGitHub;
    //    private final LinkTypeMetric linkTypeMetric;
    private final CacheManager cacheManager;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "links", key = "#telegramChatId + (#tag != null ? ':' + #tag : '')")
    public ListLinksResponse getAllLinksByChatIdAndTag(long telegramChatId, String tag) {
        Chat chat = chatRepository.findFirstByTelegramChatId(telegramChatId);

        if (chat == null) {
            log.info("Чата с id {} не существует", telegramChatId);
            throw new ChatNotFoundException(telegramChatId);
        }
        // TODO: тут можно сделать запрос уже, чтоб тег тоже передавался в запрос и возвращалось меньше данных
        List<ParseRequest> parseRequests = parseRequestRepository.findAllByChatTelegramChatId(telegramChatId);
        List<LinkResponse> linkResponses = new ArrayList<>();

        for (ParseRequest parseRequest : parseRequests) {
            // TODO: тогда тут уже такую обработку делать не надо будет
            if (tag == null || tag.equalsIgnoreCase(parseRequest.tagName())) {
                linkResponses.add(
                        new LinkResponse(telegramChatId, parseRequest.link().url()));
            }
        }

        log.info(
                "Получение всех отлеживаемых ссылок в чате с id {}, тэг: {}",
                telegramChatId,
                tag == null ? "отсутствует" : tag);
        return new ListLinksResponse(linkResponses);
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                @CacheEvict(value = "links", key = "#telegramChatId + ':' + #addLinkRequest.tag"),
                @CacheEvict(value = "links", key = "#telegramChatId")
            })
    public LinkResponse addLinkForChat(long telegramChatId, AddLinkRequest addLinkRequest) {
        if (addLinkRequest == null
                || addLinkRequest.link() == null
                || addLinkRequest.link().isBlank()) {
            log.info("Неверное тело запроса, чат с id {}", telegramChatId);
            throw new EmptyLinkException();
        }

        Chat chat = chatRepository.findFirstByTelegramChatId(telegramChatId);

        if (chat == null) {
            log.info("Чата с id {} не существует", telegramChatId);
            throw new ChatNotFoundException(telegramChatId);
        }

        String url = addLinkRequest.link();
        LocalDateTime lastAction = LocalDateTime.now(ZoneId.of("UTC"));
        Link link = linkRepository.findFirstByUrl(url);

        if (link == null) {
            ParsedLink parsedLink = LinkParser.parseLinkType(url);

            if (parsedLink.type() == LinkType.STACK_OVER_FLOW) {
                lastAction = Instant.ofEpochSecond(restClientStackOverFlow.getLastAnswerDate(parsedLink.id()))
                        .atZone(ZoneOffset.UTC)
                        .toLocalDateTime();
                link = Link.builder().url(url).lastUpdate(lastAction).build();
            } else if (parsedLink.type() == LinkType.GITHUB) {
                lastAction = restClientGitHub.getLastAction(parsedLink.owner(), parsedLink.id());
                link = Link.builder().url(url).lastUpdate(lastAction).build();
            } else {
                throw new UnknownLinkTypeException(url);
            }
            link = linkRepository.save(link);

            saveParseRequest(chat, link, lastAction, addLinkRequest);
            //            linkTypeMetric.change(parsedLink.type(), 1);
            log.info("Ссылка {} добавилась в базу и началось её отслеживание {}", url, telegramChatId);
            return new LinkResponse(telegramChatId, url);
        } else {
            ParseRequest parseRequest = parseRequestRepository.findFirstByChatIdAndLinkId(chat.id(), link.id());
            if (parseRequest == null) {
                saveParseRequest(chat, link, lastAction, addLinkRequest);
                log.info(
                        "Ссылка {} уже имелась в базе, так что она была просто добавлена для чата с id {}",
                        url,
                        telegramChatId);
                return new LinkResponse(telegramChatId, url);
            } else {
                log.info("Ссылка {} уже отслеживается в чате с id {}", url, telegramChatId);
                throw new LinkAlreadyTrackedException(url);
            }
        }
    }

    private void saveParseRequest(Chat chat, Link link, LocalDateTime lastView, AddLinkRequest addLinkRequest) {
        ParseRequest parseRequest = ParseRequest.builder()
                .chat(chat)
                .link(link)
                .tagName(addLinkRequest.tag())
                .lastView(lastView)
                .filters(new ArrayList<>())
                .build();
        for (String filter : addLinkRequest.filters()) {
            parseRequest
                    .filters()
                    .add(ParseRequestFilter.builder()
                            .parseRequest(parseRequest)
                            .filter(filter)
                            .build());
        }
        parseRequestRepository.save(parseRequest);
    }

    @Override
    @Transactional
    public LinkResponse deleteLinkFromChat(long telegramChatId, RemoveLinkRequest removeLinkRequest) {
        if (removeLinkRequest == null
                || removeLinkRequest.link() == null
                || removeLinkRequest.link().isBlank()) {
            log.info("Неверное тело запроса, чат с id {}", telegramChatId);
            throw new EmptyLinkException();
        }
        String url = removeLinkRequest.link();
        Chat chat = chatRepository.findFirstByTelegramChatId(telegramChatId);

        if (chat == null) {
            log.info("Чата с id {} не существует", telegramChatId);
            throw new ChatNotFoundException(telegramChatId);
        }

        ParseRequest parseRequest = parseRequestRepository.findByChatIdAndLinkUrl(chat.id(), url);
        Cache cache = cacheManager.getCache("links");
        if (Objects.nonNull(cache)) {
            if (parseRequest != null) {
                cache.evict(telegramChatId + ":" + parseRequest.tagName());
            }
            cache.evict(telegramChatId);
        }

        int deletedRows = parseRequestRepository.deleteByChatIdAndLinkUrl(chat.id(), url);

        if (deletedRows != 0) {
            log.info("Ссылка {} больше не отслеживается в чате с id {}", url, telegramChatId);
            int count = parseRequestRepository.countByLinkUrl(url);
            if (count == 0) {
                linkRepository.deleteByUrl(url);
                //                linkTypeMetric.change(LinkParser.parseLinkType(url).type(), -1);
            }
            return new LinkResponse(telegramChatId, url);
        } else {
            log.info("Этой ссылки нет среди отслеживаемых в чате с id {}", telegramChatId);
            throw new LinkNotFoundException(url);
        }
    }
}
