# Logic Apps Standard sample

This sample demonstrates an asynchronous flow without application code:

```text
Service Bus topic subscription
    -> stateful Logic Apps Standard workflow
        -> Azure Tables upsert
```

It uses the Logic Apps workflow runtime hosted by Azure Functions through
`Microsoft.Azure.Functions.ExtensionBundle.Workflows`.

## Workflow behavior

`MessageWriter\workflow.json`:

1. Receives each Service Bus topic message with the built-in
   `receiveTopicMessages` trigger.
2. Splits a received batch into one stateful workflow run per message.
3. Parses the unchanged JSON body from `contentData`.
4. Validates the configured partition-key and row-key fields.
5. Calls the built-in Azure Tables `upsertEntity` action.

The message is completed only after the workflow succeeds. Parsing, validation,
or storage failures fail the workflow, allowing Service Bus retry and
dead-letter behavior to remain in control.

## Open and run locally

Install these VS Code extensions:

- Azure Logic Apps (Standard)
- Azure Functions

Then:

```powershell
Copy-Item local.settings.json.example local.settings.json
code .
```

Replace `serviceBus_connectionString` in `local.settings.json`. The sample uses
Azurite for the workflow host and Azure Tables by default, so start Azurite
with table support or replace `tableStorage_connectionString` with an Azure
Storage connection string.

Create the `MessageProjection` table before starting the workflow. Use the Logic
Apps extension's **Run/Debug** command to start the local Functions-based
workflow runtime.

## Message contract

The defaults expect top-level `partitionId` and `entityId` example fields.
Change these settings to match the adopting application's contract:

```text
MessagePartitionKeyField
MessageRowKeyField
```

The entity stores the original message as `Payload` and adds Service Bus
diagnostic metadata. The complete sample message is in
`samples\message.json`.

Azure Tables keys cannot contain `/`, `\`, `#`, `?`, or control characters.
This no-code sample intentionally fails such writes rather than silently
changing identifiers. Add a transformation action only when every writer and
reader applies the same deterministic key encoding.

## Adapting the sample

This workflow is reference material, not a production implementation. Review
the schema, key strategy, retry behavior, monitoring, identity, and deployment
settings before reuse.

## Production configuration

Keep connection strings out of source control. Supply the app settings through
Key Vault references or recreate the two built-in connections with the Logic
Apps designer using managed identity.

Required access for the Logic App identity:

- `Azure Service Bus Data Receiver`
- `Storage Table Data Contributor`

Do not run this workflow and either Java receiver against the same Service Bus
subscription. They would be competing consumers. Use a separate subscription
for side-by-side evaluation.

## Kubernetes option

For a supported Kubernetes deployment, use Logic Apps Standard **Hybrid** on an
Azure Arc-enabled Kubernetes cluster. See
[`kubernetes\README.md`](kubernetes/README.md).
