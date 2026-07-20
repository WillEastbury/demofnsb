# AttributeStatusService Azure design

This repository defines the Azure replacement for the GCP-based
`AttributeStatusService`.

Both target implementations preserve the existing asynchronous behavior:

```text
Site Search API
    -> Azure Service Bus topic
        -> Attribute Status subscription
            -> Azure Function
                -> Azure Table Storage or MongoDB
                    <- CC API
```

See [the Azure implementation decision](docs/attribute-status-service-azure.md)
for the component mapping, processing behavior, security model, and deployment
requirements.

Runnable Java 21 container implementations and Kubernetes manifests are in
[implementations/java](implementations/java/README.md).

A no-code Logic Apps Standard sample using the Workflows Functions extension
bundle is in
[implementations/logic-app-standard](implementations/logic-app-standard/README.md).
