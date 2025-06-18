package backend.academy.scrapper.handler.scheduler;

import backend.academy.scrapper.model.LinkType;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import java.util.List;

public interface LinkUpdateHandler {
    boolean supports(LinkType type);

    void handle(Link link, List<ParseRequest> requests);
}
