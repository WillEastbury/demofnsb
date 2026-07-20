FROM mcr.microsoft.com/azure-functions/node:4-node20

ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true \
    AzureWebJobsFeatureFlags=EnableMultiLanguageWorker \
    AzureWebJobsSecretStorageType=Files \
    FUNCTIONS_WORKER_RUNTIME=node \
    FUNCTIONS_EXTENSION_VERSION=~4 \
    APP_KIND=workflowapp \
    WEBSITE_SITE_NAME=message-writer-logic-app

COPY host.json connections.json parameters.json /home/site/wwwroot/
COPY MessageWriter /home/site/wwwroot/MessageWriter
