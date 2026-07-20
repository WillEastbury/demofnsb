package com.willeastbury.demofnsb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AttributeStatusEventParserTest {
    @Test
    void parsesDefaultStyleKeys() {
        AttributeStatusEventParser parser =
            new AttributeStatusEventParser("siteId", "attributeId");

        AttributeStatusEvent event = parser.parse(
            """
            {
              "siteId": "site-1",
              "attributeId": "attribute-2",
              "status": "ready"
            }
            """);

        assertEquals("site-1", event.partitionKey());
        assertEquals("attribute-2", event.rowKey());
        assertEquals("ready", event.payload().get("status").asText());
    }

    @Test
    void parsesJsonPointerKeys() {
        AttributeStatusEventParser parser =
            new AttributeStatusEventParser(
                "/site/id",
                "/attribute/id");

        AttributeStatusEvent event = parser.parse(
            """
            {
              "site": { "id": 42 },
              "attribute": { "id": "colour" }
            }
            """);

        assertEquals("42", event.partitionKey());
        assertEquals("colour", event.rowKey());
    }

    @Test
    void rejectsMissingKeys() {
        AttributeStatusEventParser parser =
            new AttributeStatusEventParser("siteId", "attributeId");

        assertThrows(
            IllegalArgumentException.class,
            () -> parser.parse("{\"siteId\":\"site-1\"}"));
    }
}
