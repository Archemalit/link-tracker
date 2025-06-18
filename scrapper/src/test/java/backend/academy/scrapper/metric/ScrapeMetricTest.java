package backend.academy.scrapper.metric;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.scrapper.IntegrationEnvironment;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ScrapeMetricTest extends IntegrationEnvironment {
    private MeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        Metrics.globalRegistry.add(meterRegistry);
    }

    @AfterEach
    void tearDown() {
        meterRegistry.clear();
        Metrics.globalRegistry.clear();
    }

    @Test
    @DisplayName("Проверка работы метрик на время обработки ссылок")
    void handle() throws InterruptedException {
        // GIVEN
        ScrapeMetric scrapeMetric = new ScrapeMetric(meterRegistry);
        Sample start = scrapeMetric.start();
        Thread.sleep(1000);
        scrapeMetric.stop("github", start);

        // EXPECT
        Timer timer =
                meterRegistry.find("scrape.duration").tag("type", "github").timer();
        assertThat(timer).isNotNull();
        assertThat(timer.count()).isEqualTo(1);
        assertThat(timer.totalTime(TimeUnit.SECONDS)).isGreaterThan(0.9).isLessThan(1.1);
    }
}
