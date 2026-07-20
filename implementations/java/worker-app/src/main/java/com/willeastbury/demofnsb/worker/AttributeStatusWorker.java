package com.willeastbury.demofnsb.worker;

import io.micronaut.runtime.Micronaut;

public final class AttributeStatusWorker {
    private AttributeStatusWorker() {
    }

    public static void main(String[] args) {
        Micronaut.run(AttributeStatusWorker.class, args);
    }
}
