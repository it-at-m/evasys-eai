package de.muenchen.evasys.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    public enum EventType {
        TRAINING_PROCESSED,
        TRAINING_PROCESSING_FAILED,
        TRAINER_PROCESSED,
        TRAINER_PROCESSING_FAILED,
        SECONDARY_TRAINER_PROCESSED,
        SECONDARY_TRAINER_PROCESSING_FAILED,
        COURSE_PROCESSED,
        COURSE_PROCESSING_FAILED,
    }

    private final MeterRegistry meterRegistry;

    public MetricsService(final MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordEvent(final EventType eventType) {
        Counter.builder("evasys.events")
                .tags("event_type", eventType.name().toLowerCase(Locale.ROOT))
                .description("All events related to evasys processing")
                .register(meterRegistry)
                .increment();
    }
}
