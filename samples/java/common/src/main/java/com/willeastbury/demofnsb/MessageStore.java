package com.willeastbury.demofnsb;

public interface MessageStore extends AutoCloseable {
    void upsert(
        MessageEvent event,
        MessageMetadata metadata);

    @Override
    default void close() {
    }
}
