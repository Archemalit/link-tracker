package backend.academy.scrapper.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.IntegrationEnvironment;
import backend.academy.scrapper.mapper.LinkMapper;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.jpa.entity.Link;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@JdbcTest
@Import({JdbcLinkRepository.class, LinkMapper.class})
@ActiveProfiles("jdbc")
public class JdbcLinkRepositoryTest extends IntegrationEnvironment {

    @Autowired
    private LinkRepository jdbcLinkRepository;

    private Link link;

    @BeforeEach
    void setUp() {
        link = Link.builder()
                .url("https://stackoverflow.com/questions/1/ex")
                .lastUpdate(LocalDateTime.now())
                .build();
        link = jdbcLinkRepository.save(link);
    }

    @Test
    @DisplayName("Поиск ссылки по URL")
    void testFindLinkByUrl() {
        // WHEN
        Link foundLink = jdbcLinkRepository.findFirstByUrl(link.url());

        // THEN
        assertThat(foundLink).isNotNull();
        assertThat(foundLink.url()).isEqualTo(link.url());
    }

    @Test
    @DisplayName("Обновление даты последнего обновления ссылки")
    void testUpdateLinkLastUpdate() {
        // GIVEN
        LocalDateTime newDateTime = link.lastUpdate().plusHours(1);
        link.lastUpdate(newDateTime);

        // WHEN
        Link updatedLink = jdbcLinkRepository.save(link);

        // THEN
        assertThat(updatedLink).isNotNull();
        assertThat(updatedLink.lastUpdate()).isEqualTo(newDateTime);
    }

    @Test
    @DisplayName("Поиск несуществующей ссылки по URL")
    void testFindNonExistentLinkByUrl() {
        // WHEN
        Link foundLink = jdbcLinkRepository.findFirstByUrl("https://stackoverflow.com/questions/2/ex");

        // THEN
        assertThat(foundLink).isNull();
    }
}
