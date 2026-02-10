package de.muenchen.evasys.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MetricsServiceTest {

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    @BeforeEach
    void setup() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @ParameterizedTest
    @EnumSource(MetricsService.EventType.class)
    void recordEventIncrementsCounterWithCorrectNameAndTag(MetricsService.EventType eventType) {
        metricsService.recordEvent(eventType);

        Counter counter = meterRegistry.find("evasys.events")
                .tag("event_type", eventType.name().toLowerCase(Locale.ROOT))
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(1.0);
    }

    @Test
    void recordEventIncrementsCounterMultipleTimes() {
        metricsService.recordEvent(MetricsService.EventType.TRAINING_PROCESSED);
        metricsService.recordEvent(MetricsService.EventType.TRAINING_PROCESSED);
        metricsService.recordEvent(MetricsService.EventType.TRAINING_PROCESSED);

        Counter counter = meterRegistry.find("evasys.events")
                .tag("event_type", "training_processed")
                .counter();

        assertThat(counter).isNotNull();
        assertThat(counter.count()).isEqualTo(3.0);
    }

    @Test
    void recordEventCreatesSeparateCountersForDifferentEventTypes() {
        metricsService.recordEvent(MetricsService.EventType.TRAINER_PROCESSED);
        metricsService.recordEvent(MetricsService.EventType.COURSE_PROCESSED);
        metricsService.recordEvent(MetricsService.EventType.TRAINER_PROCESSED);

        Counter trainerCounter = meterRegistry.find("evasys.events")
                .tag("event_type", "trainer_processed")
                .counter();
        Counter courseCounter = meterRegistry.find("evasys.events")
                .tag("event_type", "course_processed")
                .counter();

        assertThat(trainerCounter).isNotNull();
        assertThat(trainerCounter.count()).isEqualTo(2.0);
        assertThat(courseCounter).isNotNull();
        assertThat(courseCounter.count()).isEqualTo(1.0);
    }
}
