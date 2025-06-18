package backend.academy.scrapper.repository;

import backend.academy.scrapper.repository.jpa.entity.Link;
import org.springframework.transaction.annotation.Transactional;

public interface LinkRepository {
    Link save(Link link);

    Link findFirstByUrl(String url);

    @Transactional
    int deleteByUrl(String url);
}
