package backend.academy.scrapper.mapper;

import backend.academy.scrapper.repository.jpa.entity.Chat;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import backend.academy.scrapper.repository.jpa.entity.ParseRequestFilter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class ParseRequestExtractor implements ResultSetExtractor<List<ParseRequest>> {
    @Override
    public List<ParseRequest> extractData(ResultSet rs) throws SQLException {
        Map<Long, ParseRequest> parseRequestMap = new HashMap<>();

        while (rs.next()) {
            Long requestId = rs.getLong("parse_request_id");

            ParseRequest parseRequest = parseRequestMap.computeIfAbsent(requestId, id -> {
                try {
                    return ParseRequest.builder()
                            .id(id)
                            .tagName(rs.getString("tag_name"))
                            .chat(Chat.builder()
                                    .id(rs.getLong("chat_id"))
                                    .telegramChatId(rs.getLong("telegram_chat_id"))
                                    .build())
                            .link(Link.builder()
                                    .id(rs.getLong("link_id"))
                                    .url(rs.getString("link_url"))
                                    .lastUpdate(rs.getObject("last_update", LocalDateTime.class))
                                    .build())
                            .lastView(rs.getObject("last_view", LocalDateTime.class))
                            .filters(new ArrayList<>())
                            .build();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            String filter = rs.getString("filter");
            if (filter != null) {
                parseRequest
                        .filters()
                        .add(ParseRequestFilter.builder()
                                .parseRequest(parseRequest)
                                .filter(filter)
                                .build());
            }
        }

        return new ArrayList<>(parseRequestMap.values());
    }
}
