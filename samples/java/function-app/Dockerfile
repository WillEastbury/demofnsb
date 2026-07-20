FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY function-app/pom.xml function-app/pom.xml
COPY worker-app/pom.xml worker-app/pom.xml
COPY common/src common/src
COPY function-app/src function-app/src

RUN mvn -B -pl function-app -am clean package

FROM mcr.microsoft.com/azure-functions/java:4-java21

ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true

COPY --from=build \
    /build/function-app/target/azure-functions/message-writer-function/ \
    /home/site/wwwroot/
