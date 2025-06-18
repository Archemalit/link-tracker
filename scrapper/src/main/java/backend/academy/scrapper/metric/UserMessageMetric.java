package backend.academy.scrapper.metric;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class UserMessageMetric {
    private final Counter counter;

    public UserMessageMetric(MeterRegistry meterRegistry) {
        this.counter = init(meterRegistry);
    }

    private Counter init(MeterRegistry meterRegistry) {
        return Counter.builder("user.messages.total")
                .description("Общее количество сообщений")
                .register(meterRegistry);
    }

    public void increment() {
        counter.increment();
    }

    public double count() {
        return counter.count();
    }
}
