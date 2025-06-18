package backend.academy.scrapper.mapper;

import backend.academy.scrapper.repository.jpa.entity.Chat;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper implements RowMapper<Chat> {
    @Override
    public Chat mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Chat.builder()
                .id(rs.getLong("id"))
                .telegramChatId(rs.getLong("telegram_chat_id"))
                .build();
    }
}
