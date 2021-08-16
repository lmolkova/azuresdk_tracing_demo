# Distributed Tracing demo for Azure

This is a draft of distributed tracing demo app demonstrating Azure SDKs and OpenTelemetry integration.

## Prerequisites

1. You'll need [Azure subscription and resources](https://portal.azure.com/)
   - EventHub
   - Storage
   - ApplicationInsights resource if you're running it with Azure Monitor
2. [Docker](https://docs.docker.com/get-docker/)

## Getting started

- Create `.env` file with access keys to resources content:

```env
EVENTHUB_CONNECTION_STRING= <event hub connection string, no entity name here>
EVENTHUB_NAME= <event hub name>
CHECKPOINT_STORAGE_ACCESS_KEY= <storage access key>
CHECKPOINT_STORAGE_ACCOUNT= <storage account name for checkpointing>
CHECKPOINT_CONTAINER_NAME= <storage container name for checkpointin>
CHECKPOINT_STORAGE_CONNECTION_STRING= <Full storage connection string for checkpointing, sorry>
```

If you're running it with Azure Monitor, please also add

`APPINSIGHTS_INSTRUMENTATIONKEY= <your app insights key>`

- Run demo with `docker-compose -f docker-compose_applicationinsights.yml up --build` (for AzureMonitor) or `docker-compose_jaeger.yml` (for Jaeger).
  
### Exposed endpoints

- `POST http://localhost:8080/messages?message=hi`: java application, will send message to EventHub
- `GET http://localhost:5000/weatherforecast`: .NET app
  
### Diagram

TODO
