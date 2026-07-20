package com.willeastbury.demofnsb.worker;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.willeastbury.demofnsb.MessageEvent;
import com.willeastbury.demofnsb.MessageEventParser;
import com.willeastbury.demofnsb.MessageStore;
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
public final class MessageProcessor
    implements ApplicationEventListener<StartupEvent> {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(MessageProcessor.class);

    private final MessageStore store;
    private final ServiceBusProcessorClient processor;
    private final AtomicBoolean running = new AtomicBoolean();

    public MessageProcessor(
        MessageEventParser parser,
        MessageStore store) {

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
        MessageEventParser parser,
        MessageStore store) {

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
            .topicName(Environment.required("MessageTopicName"))
            .subscriptionName(
                Environment.required(
                    "MessageSubscriptionName"))
            .receiveMode(ServiceBusReceiveMode.PEEK_LOCK)
            .disableAutoComplete()
            .maxConcurrentCalls(
                Environment.integerValue(
                    "MessageMaxConcurrentCalls",
                    1))
            .processMessage(context -> {
                var message = context.getMessage();
                try {
                    MessageEvent messageEvent = parser.parse(
                        message.getBody().toString());
                    OffsetDateTime enqueuedAt = message
                        .getEnqueuedTime()
                        .withOffsetSameInstant(ZoneOffset.UTC);

                    store.upsert(
                        messageEvent,
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
