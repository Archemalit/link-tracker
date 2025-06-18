package backend.academy.scrapper.service.impl.scheduler;

import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import backend.academy.scrapper.service.impl.LinkUpdateProcessor;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Profile("!multi")
public class SchedulerServiceOneImpl extends AbstractSchedulerService {

    public SchedulerServiceOneImpl(ParseRequestRepository repo, LinkUpdateProcessor processor) {
        super(repo, processor);
    }

    @Override
    @Scheduled(fixedRate = 10000)
    public void sendNewAnswers() {
        Map<Link, List<ParseRequest>> linkChats = getLinkChatMap();
        for (Map.Entry<Link, List<ParseRequest>> entry : linkChats.entrySet()) {
            linkUpdateProcessor.process(entry.getKey(), entry.getValue());
        }
    }
}
