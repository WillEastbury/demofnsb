package com.willeastbury.demofnsb.worker;

import com.willeastbury.demofnsb.AttributeStatusEventParser;
import com.willeastbury.demofnsb.AttributeStatusStore;
import com.willeastbury.demofnsb.StoreFactory;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public final class WorkerBeans {
    @Singleton
    AttributeStatusEventParser eventParser() {
        return AttributeStatusEventParser.fromEnvironment();
    }

    @Singleton
    AttributeStatusStore attributeStatusStore() {
        return StoreFactory.fromEnvironment();
    }
}
