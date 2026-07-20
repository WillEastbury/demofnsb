package com.willeastbury.demofnsb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;

public final class MongoMessageStore
    implements MessageStore {

    private final MongoClient client;
    private final MongoCollection<Document> collection;

    public MongoMessageStore(
        String connectionString,
        String databaseName,
        String collectionName) {

        this.client = MongoClients.create(connectionString);
        this.collection = client
            .getDatabase(databaseName)
            .getCollection(collectionName);
    }

    @Override
    public void upsert(
        MessageEvent event,
        MessageMetadata metadata) {

        Document id = new Document(
            "partitionKey",
            event.partitionKey())
            .append("rowKey", event.rowKey());

        Document replacement = new Document("_id", id)
            .append("payload", Document.parse(event.rawPayload()))
            .append("sourceMessageId", metadata.messageId());

        if (metadata.correlationId() != null) {
            replacement.append(
                "sourceCorrelationId",
                metadata.correlationId());
        }
        if (metadata.enqueuedAt() != null) {
            replacement.append(
                "sourceEnqueuedAt",
                metadata.enqueuedAt().toInstant());
        }

        collection.replaceOne(
            Filters.eq("_id", id),
            replacement,
            new ReplaceOptions().upsert(true));
    }

    @Override
    public void close() {
        client.close();
    }
}
