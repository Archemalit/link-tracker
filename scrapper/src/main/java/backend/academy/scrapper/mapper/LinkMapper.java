package backend.academy.scrapper.mapper;

import backend.academy.scrapper.repository.jpa.entity.Link;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class LinkMapper implements RowMapper<Link> {
    @Override
    public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Link.builder()
                .id(rs.getLong("id"))
                .url(rs.getString("url"))
                .lastUpdate(rs.getObject("last_update", LocalDateTime.class))
                .build();
    }
}
