package com.willeastbury.demofnsb;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record AttributeStatusEvent(
    String partitionKey,
    String rowKey,
    String rawPayload,
    ObjectNode payload) {
}
