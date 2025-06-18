package backend.academy.scrapper.metric;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScrapeMetric {
    private final MeterRegistry meterRegistry;

    public Sample start() {
        return Timer.start(meterRegistry);
    }

    public void stop(String type, Sample sample) {
        sample.stop(Timer.builder("scrape.duration")
                .tag("type", type)
                .publishPercentileHistogram()
                .register(meterRegistry));
    }
}
