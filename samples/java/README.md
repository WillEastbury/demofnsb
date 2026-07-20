# Java container samples

This folder contains two reference packaging options for a generic Service Bus
message receiver:

| Module | Runtime |
|---|---|
| `function-app` | Azure Functions Java 21 custom container |
| `worker-app` | Micronaut Java 21 `ServiceBusProcessorClient` container |
| `common` | Shared message parsing and Azure Tables/MongoDB stores |

Both options consume a configured Service Bus topic subscription and complete
the message only after a successful datastore upsert. Select the target with
`MessageTarget=tables` or `MessageTarget=mongodb`.

These projects are code samples, not production implementations. Replace the
example schema, resource names, key mapping, limits, and operational settings
before reuse.

The Micronaut worker uses dependency injection and application lifecycle
events to own the Service Bus processor and shared datastore. Micronaut
Management exposes `/health/liveness` and `/health/readiness` for Kubernetes.

## Message key configuration

The base sample accepts a JSON object. Configure the fields
that identify the current status row:

```text
MessagePartitionKeyField=partitionId
MessageRowKeyField=entityId
```

Values may be dotted paths such as `account.id`, or JSON Pointers such as
`/account/id`. Replace the defaults with the adopting application's logical
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

Run these commands from `samples\java`.

## Kubernetes

1. Replace every `REPLACE` value in `k8s\configmap.yaml` and
   `k8s\service-account.yaml`.
2. Set `MessageTarget` to `tables` or `mongodb`.
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
