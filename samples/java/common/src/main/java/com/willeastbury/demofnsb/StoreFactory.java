package com.willeastbury.demofnsb;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Locale;

public final class StoreFactory {
    private StoreFactory() {
    }

    public static MessageStore fromEnvironment() {
        String target = Environment.optional(
            "MessageTarget",
            "tables").toLowerCase(Locale.ROOT);

        return switch (target) {
            case "tables" -> createTablesStore();
            case "mongodb" -> createMongoStore();
            default -> throw new IllegalStateException(
                "MessageTarget must be tables or mongodb");
        };
    }

    private static MessageStore createTablesStore() {
        TableClientBuilder builder = new TableClientBuilder()
            .tableName(Environment.required("MessageTableName"));

        String connectionString = Environment.optional(
            "MessageTableConnectionString");
        if (connectionString != null) {
            builder.connectionString(connectionString);
        } else {
            builder
                .endpoint(
                    Environment.required("MessageTableServiceUri"))
                .credential(
                    new DefaultAzureCredentialBuilder().build());
        }

        TableClient tableClient = builder.buildClient();
        return new TableMessageStore(
            tableClient,
            Environment.booleanValue(
                "MessageEncodeTableKeys",
                false));
    }

    private static MessageStore createMongoStore() {
        return new MongoMessageStore(
            Environment.required(
                "MessageMongoConnectionString"),
            Environment.required("MessageMongoDatabase"),
            Environment.required("MessageMongoCollection"));
    }
}
