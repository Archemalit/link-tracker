package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.repository.LinkRepository;
import backend.academy.scrapper.repository.jpa.entity.Link;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jpa")
public interface JpaLinkRepository extends JpaRepository<Link, Long>, LinkRepository {}
