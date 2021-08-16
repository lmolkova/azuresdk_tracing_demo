# syntax=docker/dockerfile:1

FROM openjdk:16-alpine3.13

ARG TELEMETRY_DESTINATION
RUN apk add --no-cache bash

WORKDIR /app

COPY mvnw pom.xml downloadAgents.sh ./
COPY src ./src
COPY .mvn ./.mvn

RUN ./downloadAgents.sh $TELEMETRY_DESTINATION
RUN ./mvnw dependency:go-offline

CMD ./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-javaagent:./javaagents/agent.jar"