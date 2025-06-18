package backend.academy.scrapper.repository.jpa;

import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Profile("jpa")
public interface JpaParseRequestRepository extends JpaRepository<ParseRequest, Long>, ParseRequestRepository {}
