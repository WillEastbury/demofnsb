package com.willeastbury.demofnsb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MessageEventParserTest {
    @Test
    void parsesDefaultStyleKeys() {
        MessageEventParser parser =
            new MessageEventParser("partitionId", "entityId");

        MessageEvent event = parser.parse(
            """
            {
              "partitionId": "partition-1",
              "entityId": "entity-2",
              "status": "ready"
            }
            """);

        assertEquals("partition-1", event.partitionKey());
        assertEquals("entity-2", event.rowKey());
        assertEquals("ready", event.payload().get("status").asText());
    }

    @Test
    void parsesJsonPointerKeys() {
        MessageEventParser parser =
            new MessageEventParser(
                "/partition/id",
                "/entity/id");

        MessageEvent event = parser.parse(
            """
            {
              "partition": { "id": 42 },
              "entity": { "id": "colour" }
            }
            """);

        assertEquals("42", event.partitionKey());
        assertEquals("colour", event.rowKey());
    }

    @Test
    void rejectsMissingKeys() {
        MessageEventParser parser =
            new MessageEventParser("partitionId", "entityId");

        assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse("{\"partitionId\":\"partition-1\"}"));
    }
}
