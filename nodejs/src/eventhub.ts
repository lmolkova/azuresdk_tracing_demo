import {
  earliestEventPosition,
  EventHubConsumerClient,
  EventHubProducerClient,
  SubscriptionEventHandlers
} from "@azure/event-hubs";
import { BlobCheckpointStore } from "@azure/eventhubs-checkpointstore-blob";
import { ContainerClient } from "@azure/storage-blob";
import {
  context as otContext,
  SpanStatusCode,
  trace
} from "@opentelemetry/api";
import { getTracer } from "./tracing";
import { getEnvironmentVariable } from "./utils";

const eventHubHandlers: SubscriptionEventHandlers = {
  processEvents: async (events, context) => {
    if (events.length === 0) {
      return;
    }

    return getTracer().startActiveSpan(
      "eventhub-processEvents",
      async (span) => {
        for (const event of events) {
          console.log(`Received event: ${JSON.stringify(event.body)}`);
        }
        await context.updateCheckpoint(events[events.length - 1]);
        span.end();
      }
    );
  },
  processError: (err) => {
    getTracer().startActiveSpan("eventhub-processError", (span) => {
      span.recordException(err);
      span.setStatus({ code: SpanStatusCode.ERROR });
      span.end();
    });
    return Promise.reject(err);
  }
};

export function initializeEventHub() {
  const containerClient = new ContainerClient(
    getEnvironmentVariable("STORAGE_CONNECTION_STRING"),
    getEnvironmentVariable("STORAGE_CONTAINER_NAME")
  );

  const consumer = new EventHubConsumerClient(
    getEnvironmentVariable("EVENTHUB_CONSUMER_GROUP"),
    getEnvironmentVariable("EVENTHUB_CONNECTION_STRING"),
    getEnvironmentVariable("EVENTHUB_NAME"),
    new BlobCheckpointStore(containerClient)
  );

  const producer = new EventHubProducerClient(
    getEnvironmentVariable("EVENTHUB_CONNECTION_STRING"),
    getEnvironmentVariable("EVENTHUB_NAME")
  );

  consumer.subscribe(eventHubHandlers);

  return { consumer, producer };
}
