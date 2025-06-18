package backend.academy.scrapper.repository.jdbc;

import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.jpa.entity.Link;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Profile("jdbc")
public class JdbcLinkRepository implements LinkRepository {
    private static final String SAVE_LINK = "INSERT INTO link (url, last_update) VALUES (?, ?) RETURNING id";
    private static final String UPDATE_LAST_UPDATE_LINK_BY_ID = "UPDATE link SET last_update = ? WHERE id = ?";
    private static final String FIND_FIRST_LINK_BY_URL = "SELECT * FROM link WHERE url = ? LIMIT 1";
    private static final String DELETE_LINK_BY_URL = "DELETE FROM link WHERE url = ?";

    private final JdbcTemplate jdbcTemplate;
    private final LinkMapper linkMapper;

    @Override
    public Link save(Link link) {
        Long initialId = link.id();

        if (initialId == null) {
            Long linkId = jdbcTemplate.queryForObject(SAVE_LINK, Long.class, link.url(), link.lastUpdate());
            link.id(linkId);
        } else {
            jdbcTemplate.update(UPDATE_LAST_UPDATE_LINK_BY_ID, link.lastUpdate(), link.id());
        }

        return link;
    }

    @Override
    public Link findFirstByUrl(String url) {
        try {
            return jdbcTemplate.queryForObject(FIND_FIRST_LINK_BY_URL, linkMapper, url);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public int deleteByUrl(String url) {
        return jdbcTemplate.update(DELETE_LINK_BY_URL, url);
    }
}
