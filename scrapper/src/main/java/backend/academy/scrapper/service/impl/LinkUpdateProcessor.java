package backend.academy.scrapper.service.impl;

import backend.academy.scrapper.handler.scheduler.LinkUpdateHandler;
import backend.academy.scrapper.metric.ScrapeMetric;
import backend.academy.scrapper.model.ParsedLink;
import backend.academy.scrapper.repository.jpa.entity.Link;
import backend.academy.scrapper.repository.jpa.entity.ParseRequest;
import backend.academy.scrapper.util.LinkParser;
import io.micrometer.core.instrument.Timer.Sample;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkUpdateProcessor {

    private final List<LinkUpdateHandler> handlers;
    private final ScrapeMetric scrapeMetric;

    public void process(Link link, List<ParseRequest> requests) {
        ParsedLink parsed = LinkParser.parseLinkType(link.url());
        Sample sample = scrapeMetric.start();
        handlers.stream()
                .filter(handler -> handler.supports(parsed.type()))
                .findFirst()
                .ifPresent(handler -> handler.handle(link, requests));
        scrapeMetric.stop(parsed.type().name(), sample);
    }
}
