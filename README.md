# Azure Service Bus message persistence samples

This repository contains reference samples for consuming JSON messages from an
Azure Service Bus topic subscription and persisting them to Azure Table Storage
or MongoDB.

These samples demonstrate an asynchronous message-processing pattern:

```text
Message producer
    -> Azure Service Bus topic
        -> Topic subscription
            -> Sample receiver
                -> Azure Table Storage or MongoDB
                    <- Querying application
```

This is sample code, not a production implementation. Resource names, message
schema, key mapping, retry policy, monitoring, security, scaling, and deployment
settings must be reviewed for each adopting system.

See [the architecture notes](docs/azure-message-persistence-sample.md) for the
component mapping and trade-offs.

Runnable Java 21 samples and Kubernetes manifests are in
[samples/java](samples/java/README.md).

A Logic Apps Standard sample using the Workflows Functions extension bundle is
in [samples/logic-app-standard](samples/logic-app-standard/README.md).
