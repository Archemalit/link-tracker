package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.mapper.ParseRequestExtractor;
import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Profile("jdbc")
public class JdbcParseRequestRepository implements ParseRequestRepository {
    private static final String SAVE_PARSE_REQUEST =
            "INSERT INTO parse_request (chat_id, link_id, tag_name, last_view) VALUES (?, ?, ?, ?) RETURNING id";
    private static final String UPDATE_LAST_VIEW_PARSE_REQUEST_BY_ID =
            "UPDATE parse_request SET last_view = ? WHERE id = ?";
    private static final String SAVE_FILTER =
            "INSERT INTO parse_request_filters (parse_request_id, filter) VALUES (?, ?)";
    private static final String FIND_FIRST_BY_CHAT_AND_LINK_ID =
            """
        SELECT
            pr.id AS parse_request_id,
            pr.tag_name,
            c.id AS chat_id,
            c.telegram_chat_id,
            l.id AS link_id,
            l.url AS link_url,
            l.last_update,
            pr.last_view,
            prf.filter
        FROM parse_request pr
        JOIN chat c ON pr.chat_id = c.id
        JOIN link l ON pr.link_id = l.id
        LEFT JOIN parse_request_filters prf ON pr.id = prf.parse_request_id
        WHERE pr.chat_id = ? AND pr.link_id = ?
        """;
    private static final String FIND_FIRST_BY_CHAT_ID_AND_LINK_URL =
            """
        SELECT
            pr.id AS parse_request_id,
            pr.tag_name,
            c.id AS chat_id,
            c.telegram_chat_id,
            l.id AS link_id,
            l.url AS link_url,
            l.last_update,
            pr.last_view,
            prf.filter
        FROM parse_request pr
        JOIN chat c ON pr.chat_id = c.id
        JOIN link l ON pr.link_id = l.id
        LEFT JOIN parse_request_filters prf ON pr.id = prf.parse_request_id
        WHERE pr.chat_id = ? AND l.url = ?
        """;
    private static final String FIND_ALL_BY_CHAT =
            """
        SELECT
            pr.id AS parse_request_id,
            pr.tag_name,
            c.id AS chat_id,
            c.telegram_chat_id,
            l.id AS link_id,
            l.url AS link_url,
            l.last_update,
            pr.last_view,
            prf.filter
        FROM parse_request pr
        JOIN chat c ON pr.chat_id = c.id
        JOIN link l ON pr.link_id = l.id
        LEFT JOIN parse_request_filters prf ON pr.id = prf.parse_request_id
        WHERE c.telegram_chat_id = ?
        """;
    private static final String FIND_ALL_LINK_CHATS =
            """
            SELECT
                        pr.id AS parse_request_id,
                        pr.tag_name,
                        c.id AS chat_id,
                        c.telegram_chat_id,
                        l.id AS link_id,
                        l.url AS link_url,
                        l.last_update,
                        pr.last_view,
                        prf.filter
                    FROM parse_request pr
                    JOIN chat c ON pr.chat_id = c.id
                    JOIN link l ON pr.link_id = l.id
                    LEFT JOIN parse_request_filters prf ON pr.id = prf.parse_request_id
                                    """;
    private static final String COUNT_LINK_BY_LINK_URL =
            """
        SELECT COUNT(*)
        FROM parse_request pr
        JOIN link l ON pr.link_id = l.id
        WHERE l.url = ?
        """;
    private static final String DELETE_BY_CHAT_AND_LINK_URL =
            """
            DELETE FROM parse_request
            WHERE chat_id = ? AND link_id = (SELECT id FROM link WHERE url = ?)
        """;
    private static final String DELETE_BY_CHAT_AND_LINK_ID =
            """
            DELETE FROM parse_request WHERE chat_id = ? AND link_id = ?
        """;

    private final JdbcTemplate jdbcTemplate;
    private final ParseRequestExtractor parseRequestExtractor;

    @Override
    public ParseRequest save(ParseRequest parseRequest) {
        Long initialId = parseRequest.id();

        if (initialId == null) {
            Long parseRequestId = jdbcTemplate.queryForObject(
                    SAVE_PARSE_REQUEST,
                    Long.class,
                    parseRequest.chat().id(),
                    parseRequest.link().id(),
                    parseRequest.tagName(),
                    parseRequest.lastView());
            parseRequest.id(parseRequestId);
            if (parseRequest.filters() != null) {
                List<Object[]> batchArgs = parseRequest.filters().stream()
                        .map(filter -> new Object[] {parseRequestId, filter.filter()})
                        .toList();
                jdbcTemplate.batchUpdate(SAVE_FILTER, batchArgs);
            }
        } else {
            jdbcTemplate.update(UPDATE_LAST_VIEW_PARSE_REQUEST_BY_ID, parseRequest.lastView(), initialId);
        }

        return parseRequest;
    }

    @SuppressFBWarnings(
            value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "Возврат null — это ожидаемое поведение, обрабатывается вызывающей стороной")
    @Override
    public ParseRequest findFirstByChatIdAndLinkId(Long chatId, Long linkId) {
        List<ParseRequest> result =
                jdbcTemplate.query(FIND_FIRST_BY_CHAT_AND_LINK_ID, parseRequestExtractor, chatId, linkId);

        return result.isEmpty() ? null : result.getFirst();
    }

    @SuppressFBWarnings(
            value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE",
            justification = "Возврат null — это ожидаемое поведение, обрабатывается вызывающей стороной")
    @Override
    public ParseRequest findByChatIdAndLinkUrl(Long chatId, String linkUrl) {
        List<ParseRequest> result =
                jdbcTemplate.query(FIND_FIRST_BY_CHAT_ID_AND_LINK_URL, parseRequestExtractor, chatId, linkUrl);

        return result.isEmpty() ? null : result.getFirst();
    }

    @Override
    public List<ParseRequest> findAllByChatTelegramChatId(Long telegramChatId) {
        return jdbcTemplate.query(FIND_ALL_BY_CHAT, parseRequestExtractor, telegramChatId);
    }

    @Override
    public List<ParseRequest> findAll() {
        return jdbcTemplate.query(FIND_ALL_LINK_CHATS, parseRequestExtractor);
    }

    @Override
    public int countByLinkUrl(String linkUrl) {
        return jdbcTemplate.update(COUNT_LINK_BY_LINK_URL, linkUrl);
    }

    @Override
    public int deleteByChatIdAndLinkUrl(Long chatId, String linkUrl) {
        return jdbcTemplate.update(DELETE_BY_CHAT_AND_LINK_URL, chatId, linkUrl);
    }

    @Override
    public void deleteByChatIdAndLinkId(Long chatId, Long linkId) {
        jdbcTemplate.update(DELETE_BY_CHAT_AND_LINK_ID, chatId, linkId);
    }
}
