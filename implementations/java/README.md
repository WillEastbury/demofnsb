# Java container implementations

This folder contains two packaging options for the same
`AttributeStatusService` receiver:

| Module | Runtime |
|---|---|
| `function-app` | Azure Functions Java 21 custom container |
| `worker-app` | Micronaut Java 21 `ServiceBusProcessorClient` container |
| `common` | Shared message parsing and Azure Tables/MongoDB stores |

Both options consume the existing Service Bus topic subscription and complete
the message only after a successful datastore upsert. Select the target with
`AttributeStatusTarget=tables` or `AttributeStatusTarget=mongodb`.

The Micronaut worker uses dependency injection and application lifecycle
events to own the Service Bus processor and shared datastore. Micronaut
Management exposes `/health/liveness` and `/health/readiness` for Kubernetes.

## Message key configuration

The base implementation accepts an unchanged JSON object. Configure the fields
that identify the current status row:

```text
AttributeStatusPartitionKeyField=siteId
AttributeStatusRowKeyField=attributeId
```

Values may be dotted paths such as `site.id`, or JSON Pointers such as
`/site/id`. Replace the defaults with the existing event contract's logical
keys.

The complete original JSON is retained in the `Payload` property in Azure
Tables or the `payload` document in MongoDB. The deterministic logical key
makes redelivery idempotent. Azure Tables payloads must remain below the
service's single-string property limit. Provision the table before starting
either container.

## Build

Build and test all modules with Maven:

```powershell
mvn clean verify
```

The Docker builds include Maven, so a local Maven installation is not required:

```powershell
docker build -f function-app\Dockerfile `
  -t ghcr.io/willeastbury/demofnsb-function:latest .

docker build -f worker-app\Dockerfile `
  -t ghcr.io/willeastbury/demofnsb-worker:latest .
```

Run these commands from `implementations\java`.

## Kubernetes

1. Replace every `REPLACE` value in `k8s\configmap.yaml` and
   `k8s\service-account.yaml`.
2. Set `AttributeStatusTarget` to `tables` or `mongodb`.
3. If MongoDB does not use workload identity, copy
   `k8s\secret.example.yaml`, supply the connection string through your secret
   management process, and apply the resulting Secret.
4. Grant the workload identity `Azure Service Bus Data Receiver`. For Tables,
   also grant `Storage Table Data Contributor`. The Functions container also
   needs the storage data roles required by its `AzureWebJobsStorage` host
   account.
5. Apply the common resources and exactly one runtime Deployment:

```powershell
kubectl apply -f k8s\namespace.yaml
kubectl apply -f k8s\service-account.yaml
kubectl apply -f k8s\configmap.yaml

# Choose one:
kubectl apply -f k8s\function-deployment.yaml
# or:
kubectl apply -f k8s\worker-deployment.yaml
```

Do not run both Deployments against the same subscription. They would be
competing consumers, and each message would be processed by only one of them.
To compare both runtimes or datastores, create a second Service Bus
subscription.
