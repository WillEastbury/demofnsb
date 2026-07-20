package com.willeastbury.demofnsb.worker;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.management.health.indicator.annotation.Readiness;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

@Singleton
@Readiness
public final class ServiceBusReadinessIndicator
    implements HealthIndicator {

    private final AttributeStatusProcessor processor;

    public ServiceBusReadinessIndicator(
        AttributeStatusProcessor processor) {

        this.processor = processor;
    }

    @Override
    public Publisher<HealthResult> getResult() {
        HealthStatus status = processor.isRunning()
            ? HealthStatus.UP
            : HealthStatus.DOWN;

        return Publishers.just(
            HealthResult.builder("service-bus", status).build());
    }
}
