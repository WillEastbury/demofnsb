# Running the Logic Apps runtime on Kubernetes with KEDA

This sample packages the Logic Apps Standard workflow in an Azure Functions
Node 20 container and uses KEDA to scale the runtime from the Service Bus topic
subscription.

This is an experimental, best-effort deployment model rather than the fully
managed Logic Apps Standard service.

```text
KEDA Service Bus scaler
    -> scales Deployment from 0..N
        -> Functions host + Workflows extension bundle
            -> MessageWriter workflow
                -> Azure Tables
```

KEDA only reads subscription metrics. The Logic Apps Service Bus trigger
receives and settles the messages.

## Prerequisites

- A Kubernetes cluster.
- KEDA 2.x installed in the cluster.
- A container registry accessible by the cluster.
- An existing Service Bus topic and subscription.
- An existing Azure Table.
- Azure Storage for `AzureWebJobsStorage`.
- An Application Insights connection string. The Workflows extension requires
  telemetry registration during host startup.

Install KEDA with Helm:

```powershell
helm repo add kedacore https://kedacore.github.io/charts
helm repo update
helm upgrade --install keda kedacore/keda `
  --namespace keda `
  --create-namespace
```

## Build and publish

Run from `samples\logic-app-standard`:

```powershell
docker build -t ghcr.io/willeastbury/demofnsb-logic-app:latest .
docker push ghcr.io/willeastbury/demofnsb-logic-app:latest
```

Replace the image in `deployment.yaml` if a different registry or tag is used.

## Configure secrets

Copy `secret.example.yaml` to a file outside source control and replace every
placeholder.

The sample separates Service Bus credentials:

- `serviceBus_connectionString` is used by the workflow and needs **Listen**.
- `kedaServiceBusConnectionString` is used only by KEDA and needs **Manage** so
  the scaler can read subscription runtime metrics.

`TriggerAuthentication` reads the KEDA credential directly from the Secret. It
is not injected into the workflow container.

For a local-only smoke test, an all-zero instrumentation key is sufficient to
register telemetry services. Use a real Application Insights connection string
for a deployed environment.

## Deploy

The topic and subscription names in `configmap.yaml` and `scaledobject.yaml`
must match.

```powershell
kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f secret.local.yaml
kubectl apply -f deployment.yaml
kubectl apply -f trigger-authentication.yaml
kubectl apply -f scaledobject.yaml
```

The Deployment starts with zero replicas. Publishing a message activates KEDA,
which starts the workflow host. After the subscription is drained and the
cooldown period expires, KEDA scales the Deployment back to zero.

Inspect the deployment:

```powershell
kubectl get scaledobject,hpa,deploy,pods `
  --namespace message-persistence-sample

kubectl logs deployment/message-writer-logic-app `
  --namespace message-persistence-sample `
  --follow
```

## Scaling settings

`scaledobject.yaml` uses:

- `messageCount: "5"`: desired active messages per replica.
- `minReplicaCount: 0`: scale to zero when idle.
- `maxReplicaCount: 5`: sample safety cap.
- `pollingInterval: 15`: Service Bus metric polling interval.
- `cooldownPeriod: 60`: idle time before scaling to zero.

Tune these values for the adopting workload.

## Runtime considerations

- All replicas must share the same `AzureWebJobsStorage` account.
- All replicas should share the same Application Insights resource.
- Stateful workflow execution and trigger coordination depend on that storage.
- Extension-bundle download can make the first cold start slower.
- Built-in connectors are the safest choice in this hosting model.
- Rebuild and redeploy the image for workflow changes.
- Use a separate subscription when comparing this runtime with another sample.

Microsoft describes Kubernetes-hosted Azure Functions with KEDA as an
open-source, community-supported model:

- [Azure Functions on Kubernetes with KEDA](https://learn.microsoft.com/azure/azure-functions/functions-kubernetes-keda)
- [KEDA Azure Service Bus scaler](https://keda.sh/docs/2.18/scalers/azure-service-bus/)
