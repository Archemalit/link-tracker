// package backend.academy.scrapper.metric;
//
// import backend.academy.scrapper.model.LinkType;
// import io.micrometer.core.instrument.Gauge;
// import io.micrometer.core.instrument.MeterRegistry;
// import java.util.concurrent.atomic.AtomicLong;
// import org.springframework.stereotype.Component;
//
// @Component
// public class LinkTypeMetric {
//    private AtomicLong countGitHub;
//    private AtomicLong countStackOverFlow;
//
//    public LinkTypeMetric(MeterRegistry meterRegistry) {
//        init(meterRegistry);
//    }
//
//    private void init(MeterRegistry meterRegistry) {
//        countGitHub = new AtomicLong(0);
//        Gauge.builder("link.active.count", countGitHub, AtomicLong::get)
//            .tag("type", "github")
//            .register(meterRegistry);
//
//        countStackOverFlow = new AtomicLong(0);
//        Gauge.builder("link.active.count", countStackOverFlow, AtomicLong::get)
//            .tag("type", "stackoverflow")
//            .register(meterRegistry);
//    }
//
//    public void change(LinkType linkType, long value) {
//        if (linkType == LinkType.GITHUB) {
//            countGitHub.addAndGet(value);
//        } else if (linkType == LinkType.STACK_OVER_FLOW) {
//            countStackOverFlow.addAndGet(value);
//        }
//    }
// }
