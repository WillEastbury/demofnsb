package com.willeastbury.demofnsb;

public interface AttributeStatusStore extends AutoCloseable {
    void upsert(
        AttributeStatusEvent event,
        MessageMetadata metadata);

    @Override
    default void close() {
    }
}
