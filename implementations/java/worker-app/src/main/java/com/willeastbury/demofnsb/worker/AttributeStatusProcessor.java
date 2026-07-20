package com.willeastbury.demofnsb.worker;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.willeastbury.demofnsb.AttributeStatusEvent;
import com.willeastbury.demofnsb.AttributeStatusEventParser;
import com.willeastbury.demofnsb.AttributeStatusStore;
import com.willeastbury.demofnsb.Environment;
import com.willeastbury.demofnsb.MessageMetadata;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class AttributeStatusProcessor
    implements ApplicationEventListener<StartupEvent> {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(AttributeStatusProcessor.class);

    private final AttributeStatusStore store;
    private final ServiceBusProcessorClient processor;
    private final AtomicBoolean running = new AtomicBoolean();

    public AttributeStatusProcessor(
        AttributeStatusEventParser parser,
        AttributeStatusStore store) {

        this.store = store;
        this.processor = createProcessor(parser, store);
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        processor.start();
        running.set(true);
        LOGGER.info("Attribute status Service Bus processor started");
    }

    public boolean isRunning() {
        return running.get();
    }

    @PreDestroy
    void close() {
        running.set(false);
        processor.close();
        store.close();
    }

    private static ServiceBusProcessorClient createProcessor(
        AttributeStatusEventParser parser,
        AttributeStatusStore store) {

        ServiceBusClientBuilder builder = new ServiceBusClientBuilder();
        String connectionString = Environment.optional(
            "ServiceBusConnectionString");

        if (connectionString != null) {
            builder.connectionString(connectionString);
        } else {
            String namespace = Environment.optional(
                "ServiceBusFullyQualifiedNamespace",
                Environment.optional(
                    "ServiceBusConnection__fullyQualifiedNamespace"));
            if (namespace == null) {
                throw new IllegalStateException(
                    "Service Bus namespace configuration is missing");
            }

            builder.credential(
                namespace,
                new DefaultAzureCredentialBuilder().build());
        }

        return builder.processor()
            .topicName(Environment.required("AttributeStatusTopicName"))
            .subscriptionName(
                Environment.required(
                    "AttributeStatusSubscriptionName"))
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()
            .maxConcurrentCalls(
                Environment.integerValue(
                    "AttributeStatusMaxConcurrentCalls",
                    1))
            .processMessage(context -> {
                var message = context.getMessage();
                try {
                    AttributeStatusEvent status = parser.parse(
                        message.getBody().toString());
                    OffsetDateTime enqueuedAt = message
                        .getEnqueuedTime()
                        .withOffsetSameInstant(ZoneOffset.UTC);

                    store.upsert(
                        status,
                        new MessageMetadata(
                            message.getMessageId(),
                            message.getCorrelationId(),
                            enqueuedAt));
                    context.complete();
                    LOGGER.info(
                        "Stored attribute status message {}",
                        message.getMessageId());
                } catch (RuntimeException exception) {
                    context.abandon();
                    throw exception;
                }
            })
            .processError(context -> LOGGER.error(
                "Service Bus processor error in namespace {}",
                context.getFullyQualifiedNamespace(),
                context.getException()))
            .buildProcessorClient();
    }
}
