package com.willeastbury.demofnsb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class AttributeStatusEventParser {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String partitionKeyField;
    private final String rowKeyField;

    public AttributeStatusEventParser(
        String partitionKeyField,
        String rowKeyField) {

        this.partitionKeyField = requireName(
            partitionKeyField,
            "partitionKeyField");
        this.rowKeyField = requireName(rowKeyField, "rowKeyField");
    }

    public static AttributeStatusEventParser fromEnvironment() {
        return new AttributeStatusEventParser(
            Environment.optional(
                "AttributeStatusPartitionKeyField",
                "siteId"),
            Environment.optional(
                "AttributeStatusRowKeyField",
                "attributeId"));
    }

    public AttributeStatusEvent parse(String rawPayload) {
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new IllegalArgumentException(
                "Attribute status message body is empty");
        }

        try {
            JsonNode parsed = OBJECT_MAPPER.readTree(rawPayload);
            if (!(parsed instanceof ObjectNode objectNode)) {
                throw new IllegalArgumentException(
                    "Attribute status message must be a JSON object");
            }

            String partitionKey = readRequiredKey(
                objectNode,
                partitionKeyField);
            String rowKey = readRequiredKey(objectNode, rowKeyField);

            return new AttributeStatusEvent(
                partitionKey,
                rowKey,
                rawPayload,
                objectNode);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException(
                "Attribute status message is not valid JSON",
                exception);
        }
    }

    private static String readRequiredKey(
        ObjectNode payload,
        String fieldPath) {

        JsonNode value = fieldPath.startsWith("/")
            ? payload.at(fieldPath)
            : readDottedPath(payload, fieldPath);

        if (value.isMissingNode() || value.isNull() || value.isContainerNode()) {
            throw new IllegalArgumentException(
                "Required scalar key field is missing: " + fieldPath);
        }

        String key = value.asText();
        if (key.isBlank()) {
            throw new IllegalArgumentException(
                "Required key field is blank: " + fieldPath);
        }
        return key;
    }

    private static JsonNode readDottedPath(
        JsonNode payload,
        String fieldPath) {

        JsonNode current = payload;
        for (String segment : fieldPath.split("\\.")) {
            current = current.path(segment);
        }
        return current;
    }

    private static String requireName(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return value;
    }
}
