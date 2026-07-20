# Running the Logic Apps sample on Kubernetes

## Supported option

The Microsoft-supported Kubernetes route is **Azure Logic Apps Standard
Hybrid**, not a hand-written Kubernetes `Deployment` for a Functions base
image.

The hybrid model runs the Logic Apps runtime through the Azure Container Apps
extension on an Azure Arc-enabled Kubernetes cluster.

```text
Azure management plane
    -> Azure Arc-enabled Kubernetes
        -> Azure Container Apps extension
            -> Logic Apps Standard Hybrid runtime
                -> MessageWriter workflow
```

## Required infrastructure

- An Azure Arc-enabled supported Kubernetes cluster.
- The Azure Container Apps extension and a connected environment.
- A custom location associated with that environment.
- An SQL database for workflow state and run history.
- An SMB file share for workflow artifacts.
- Network access from the runtime to Service Bus, Azure Tables, SQL, and SMB.

All infrastructure components must meet the regional and networking
requirements in the Microsoft documentation.

## Deployment outline

1. Create or select a supported Kubernetes cluster.
2. Connect the cluster to Azure Arc.
3. Install the Azure Container Apps extension.
4. Create the custom location and connected environment.
5. Configure the SQL storage provider and SMB artifact share.
6. In Azure, create a **Logic App (Standard) > Hybrid** resource targeting the
   connected environment.
7. Deploy this project from the Azure Logic Apps (Standard) VS Code extension:
   - Open `samples\logic-app-standard`.
   - Select **Deploy to logic app**.
   - Select the **Hybrid** hosting plan.
   - Select the connected environment.
   - Prefer zip deployment, or provide the SMB deployment settings.
8. Configure the following application settings on the hybrid Logic App:

```text
AzureWebJobsStorage
MessageTopicName
MessageSubscriptionName
MessageTableName
MessagePartitionKeyField
MessageRowKeyField
serviceBus_connectionString
tableStorage_connectionString
```

9. Create the target table before enabling the workflow.
10. Publish a copy of `samples\message.json` to the configured topic.

## Authentication limitation

Logic Apps Hybrid currently does not support managed identity authentication
for connector operations. This sample therefore uses connection-string-based
built-in connections. Store those values in the platform's protected
application settings and rotate them through the adopting environment's secret
management process.

## Why no raw Kubernetes manifest is included

A plain `Deployment` using an Azure Functions container does not provide the
supported Logic Apps Hybrid control plane, SQL state provider, SMB artifacts,
revision management, or Azure Container Apps extension integration. Such a
manifest would be an experimental container deployment rather than a supported
Logic Apps-on-Kubernetes option.

## Microsoft documentation

- [Set up infrastructure for Logic Apps Standard Hybrid](https://learn.microsoft.com/azure/logic-apps/set-up-standard-workflows-hybrid-deployment-requirements)
- [Create and deploy Standard workflows using the hybrid model](https://learn.microsoft.com/azure/logic-apps/create-standard-workflows-hybrid-deployment)
