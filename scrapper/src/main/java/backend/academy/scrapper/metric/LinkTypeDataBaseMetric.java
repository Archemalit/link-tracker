package backend.academy.scrapper.metric;

import backend.academy.scrapper.model.LinkType;
import backend.academy.scrapper.repository.ParseRequestRepository;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import backend.academy.scrapper.util.LinkParser;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LinkTypeDataBaseMetric {
    private final ParseRequestRepository parseRequestRepository;
    public AtomicLong countGitHub;
    private AtomicLong countStackOverFlow;

    public LinkTypeDataBaseMetric(MeterRegistry meterRegistry, ParseRequestRepository parseRequestRepository) {
        this.parseRequestRepository = parseRequestRepository;
        init(meterRegistry);
    }

    private void init(MeterRegistry meterRegistry) {
        countGitHub = new AtomicLong(0);
        Gauge.builder("link.active.count", countGitHub, AtomicLong::get)
                .tag("type", "github")
                .register(meterRegistry);

        countStackOverFlow = new AtomicLong(0);
        Gauge.builder("link.active.count", countStackOverFlow, AtomicLong::get)
                .tag("type", "stackoverflow")
                .register(meterRegistry);
    }

    @Scheduled(fixedRate = 60_000)
    public void update() {
        Set<Long> usedLinkIds = new HashSet<>();
        List<ParseRequest> parseRequests = parseRequestRepository.findAll();

        long gitHubCount = 0;
        long stackOverFlowCount = 0;

        for (ParseRequest parseRequest : parseRequests) {
            Long linkId = parseRequest.link().id();
            String linkUrl = parseRequest.link().url();
            if (!usedLinkIds.contains(linkId)) {
                LinkType type = LinkParser.parseLinkType(linkUrl).type();
                if (type == LinkType.GITHUB) {
                    gitHubCount++;
                } else if (type == LinkType.STACK_OVER_FLOW) {
                    stackOverFlowCount++;
                }
            }
            usedLinkIds.add(linkId);
        }
        countGitHub.set(gitHubCount);
        countStackOverFlow.set(stackOverFlowCount);
    }
}
