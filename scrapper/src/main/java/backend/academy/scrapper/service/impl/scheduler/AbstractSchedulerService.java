package backend.academy.scrapper.service.impl.scheduler;

import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import backend.academy.scrapper.service.SchedulerService;
import backend.academy.scrapper.service.impl.LinkUpdateProcessor;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractSchedulerService implements SchedulerService {
    protected final ParseRequestRepository parseRequestRepository;
    protected final LinkUpdateProcessor linkUpdateProcessor;

    protected Map<Link, List<ParseRequest>> getLinkChatMap() {
        List<ParseRequest> results = parseRequestRepository.findAll();
        return results.stream().collect(Collectors.groupingBy(ParseRequest::link));
    }
}
