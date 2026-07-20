package com.willeastbury.demofnsb;

import java.time.OffsetDateTime;

public record MessageMetadata(
    String messageId,
    String correlationId,
    OffsetDateTime enqueuedAt) {
}
