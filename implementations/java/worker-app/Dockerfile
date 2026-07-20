FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
COPY common/pom.xml common/pom.xml
COPY function-app/pom.xml function-app/pom.xml
COPY worker-app/pom.xml worker-app/pom.xml
COPY common/src common/src
COPY worker-app/src worker-app/src

RUN mvn -B -pl worker-app -am clean package

FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /app

COPY --from=build \
    /build/worker-app/target/attribute-status-worker-1.0.0-SNAPSHOT.jar \
    /app/attribute-status-worker.jar

USER app
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/attribute-status-worker.jar"]
