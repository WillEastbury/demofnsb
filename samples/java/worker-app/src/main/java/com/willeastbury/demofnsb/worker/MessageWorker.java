package com.willeastbury.demofnsb.worker;

import io.micronaut.runtime.Micronaut;

public final class MessageWorker {
    private MessageWorker() {
    }

    public static void main(String[] args) {
        Micronaut.run(MessageWorker.class, args);
    }
}
