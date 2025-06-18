package backend.academy.scrapper.repository.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.IntegrationEnvironment;
import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.jpa.entity.Link;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("jpa")
public class JpaLinkRepositoryTest extends IntegrationEnvironment {

    @Autowired
    private LinkRepository jpaLinkRepository;

    private String url;
    private Link link;

    @BeforeEach
    void setUp() {
        url = "https://stackoverflow.com/questions/123/example";
        link = Link.builder().url(url).lastUpdate(LocalDateTime.now()).build();
        link = jpaLinkRepository.save(link);
    }

    @Test
    @DisplayName("Поиск ссылки по URL")
    void testFindLinkByUrl() {
        // WHEN
        Link foundLink = jpaLinkRepository.findFirstByUrl(url);

        // THEN
        assertThat(foundLink).isNotNull();
        assertThat(foundLink.id()).isEqualTo(link.id());
        assertThat(foundLink.url()).isEqualTo(link.url());
    }
}
