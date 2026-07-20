package com.willeastbury.demofnsb.worker;

import com.willeastbury.demofnsb.MessageEventParser;
import com.willeastbury.demofnsb.MessageStore;
import com.willeastbury.demofnsb.StoreFactory;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public final class WorkerBeans {
    @Singleton
    MessageEventParser eventParser() {
        return MessageEventParser.fromEnvironment();
    }

    @Singleton
    MessageStore messageStore() {
        return StoreFactory.fromEnvironment();
    }
}
