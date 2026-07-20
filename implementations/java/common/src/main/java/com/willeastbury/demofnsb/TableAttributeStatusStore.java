package com.willeastbury.demofnsb;

import com.azure.core.util.Context;
import com.azure.data.tables.TableClient;
import com.azure.data.tables.models.TableEntity;
import com.azure.data.tables.models.TableEntityUpdateMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public final class TableAttributeStatusStore
    implements AttributeStatusStore {

    private static final int MAX_PAYLOAD_CHARACTERS = 60_000;

    private final TableClient tableClient;
    private final boolean encodeKeys;

    public TableAttributeStatusStore(
        TableClient tableClient,
        boolean encodeKeys) {

        this.tableClient = tableClient;
        this.encodeKeys = encodeKeys;
    }

    @Override
    public void upsert(
        AttributeStatusEvent event,
        MessageMetadata metadata) {

        if (event.rawPayload().length() > MAX_PAYLOAD_CHARACTERS) {
            throw new IllegalArgumentException(
                "Message payload exceeds the Azure Tables string limit");
        }

        TableEntity entity = new TableEntity(
            tableKey(event.partitionKey()),
            tableKey(event.rowKey()))
            .addProperty("Payload", event.rawPayload())
            .addProperty("SourceMessageId", metadata.messageId());

        if (metadata.correlationId() != null) {
            entity.addProperty(
                "SourceCorrelationId",
                metadata.correlationId());
        }
        if (metadata.enqueuedAt() != null) {
            entity.addProperty(
                "SourceEnqueuedAt",
                metadata.enqueuedAt());
        }

        tableClient.upsertEntityWithResponse(
            entity,
            TableEntityUpdateMode.REPLACE,
            Duration.ofSeconds(30),
            Context.NONE);
    }

    private String tableKey(String key) {
        if (encodeKeys) {
            return "b64-" + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                    key.getBytes(StandardCharsets.UTF_8));
        }

        validateTableKey(key);
        return key;
    }

    private static void validateTableKey(String key) {
        for (int index = 0; index < key.length(); index++) {
            char character = key.charAt(index);
            if (character == '/'
                || character == '\\'
                || character == '#'
                || character == '?'
                || Character.isISOControl(character)) {

                throw new IllegalArgumentException(
                    "Azure Tables key contains an invalid character; "
                        + "enable AttributeStatusEncodeTableKeys");
            }
        }
    }
}
