#!/bin/bash

mkdir ./javaagents

if [[ $1 == "applicationinsightssdk" ]]; then 
    wget https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.1.1/applicationinsights-agent-3.1.1.jar -P javaagents/ 
    mv ./javaagents/applicationinsights-agent-3.1.1.jar ./javaagents/agent.jar
    echo "{\"preview\":{\"openTelemetryApiSupport\":true,\"instrumentation\":{\"azureSdk\":{\"enabled\": true}}}}" >> ./javaagents/applicationinsights.json
else 
    wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent-all.jar -P javaagents/
    mv ./javaagents/opentelemetry-javaagent-all.jar ./javaagents/agent.jar
fi
