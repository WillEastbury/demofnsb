package com.willeastbury.demofnsb;

import com.azure.data.tables.TableClient;
import com.azure.data.tables.TableClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.util.Locale;

public final class StoreFactory {
    private StoreFactory() {
    }

    public static AttributeStatusStore fromEnvironment() {
        String target = Environment.optional(
            "AttributeStatusTarget",
            "tables").toLowerCase(Locale.ROOT);

        return switch (target) {
            case "tables" -> createTablesStore();
            case "mongodb" -> createMongoStore();
            default -> throw new IllegalStateException(
                "AttributeStatusTarget must be tables or mongodb");
        };
    }

    private static AttributeStatusStore createTablesStore() {
        TableClientBuilder builder = new TableClientBuilder()
            .tableName(Environment.required("AttributeStatusTableName"));

        String connectionString = Environment.optional(
            "AttributeStatusTableConnectionString");
        if (connectionString != null) {
            builder.connectionString(connectionString);
        } else {
            builder
                .endpoint(
                    Environment.required("AttributeStatusTableServiceUri"))
                .credential(
                    new DefaultAzureCredentialBuilder().build());
        }

        TableClient tableClient = builder.buildClient();
        return new TableAttributeStatusStore(
            tableClient,
            Environment.booleanValue(
                "AttributeStatusEncodeTableKeys",
                false));
    }

    private static AttributeStatusStore createMongoStore() {
        return new MongoAttributeStatusStore(
            Environment.required(
                "AttributeStatusMongoConnectionString"),
            Environment.required("AttributeStatusMongoDatabase"),
            Environment.required("AttributeStatusMongoCollection"));
    }
}
