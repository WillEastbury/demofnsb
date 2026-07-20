package com.willeastbury.demofnsb.function;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusTopicTrigger;
import com.willeastbury.demofnsb.AttributeStatusEvent;
import com.willeastbury.demofnsb.AttributeStatusEventParser;
import com.willeastbury.demofnsb.AttributeStatusStore;
import com.willeastbury.demofnsb.MessageMetadata;
import com.willeastbury.demofnsb.StoreFactory;

public final class AttributeStatusWriterFunction {
    private static final AttributeStatusEventParser PARSER = AttributeStatusEventParser.fromEnvironment();
    private static final AttributeStatusStore STORE = StoreFactory.fromEnvironment();
    
    @FunctionName("AttributeStatusWriter")
    public void run(
        @ServiceBusTopicTrigger( name = "message", topicName = "%AttributeStatusTopicName%", subscriptionName = "%AttributeStatusSubscriptionName%", connection = "ServiceBusConnection")
        String message, 
        @BindingName("MessageId") String messageId,
        ExecutionContext context) 
    {
        AttributeStatusEvent event = PARSER.parse(message);
        STORE.upsert(event, new MessageMetadata(messageId, null, null));
        context.getLogger().info("Stored attribute status message " + messageId);
    }
}
