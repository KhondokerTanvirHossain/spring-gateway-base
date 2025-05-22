package com.tanvir.spring_boot_mvc_jpa_base.core.util.constants.tracer;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class TracerUtil {
    private final Tracer tracer;

    public String getCurrentTraceId() {
        if (tracer.currentSpan() == null || tracer.currentSpan().context() == null) {
            throw new IllegalStateException("No current span or context available for tracing.");
        }
        return Objects.requireNonNull(tracer.currentSpan()).context().traceId();
    }
}
