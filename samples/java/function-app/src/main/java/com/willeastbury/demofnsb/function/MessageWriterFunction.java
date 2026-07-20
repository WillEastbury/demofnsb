package com.willeastbury.demofnsb.function;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusTopicTrigger;
import com.willeastbury.demofnsb.MessageEvent;
import com.willeastbury.demofnsb.MessageEventParser;
import com.willeastbury.demofnsb.MessageStore;
import com.willeastbury.demofnsb.MessageMetadata;
import com.willeastbury.demofnsb.StoreFactory;

public final class MessageWriterFunction {
    private static final MessageEventParser PARSER =
        MessageEventParser.fromEnvironment();
    private static final MessageStore STORE = StoreFactory.fromEnvironment();

    @FunctionName("MessageWriter")
    public void run(
        @ServiceBusTopicTrigger(
            name = "message",
            topicName = "%MessageTopicName%",
            subscriptionName = "%MessageSubscriptionName%",
            connection = "ServiceBusConnection")
        String message,
        @BindingName("MessageId") String messageId,
        ExecutionContext context) 
    {
        MessageEvent event = PARSER.parse(message);
        STORE.upsert(event, new MessageMetadata(messageId, null, null));
        context.getLogger().info("Stored attribute status message " + messageId);
    }
}
