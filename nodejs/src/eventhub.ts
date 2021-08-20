import {
  earliestEventPosition,
  EventHubConsumerClient,
  EventHubProducerClient,
  SubscriptionEventHandlers
} from "@azure/event-hubs";
import { BlobCheckpointStore } from "@azure/eventhubs-checkpointstore-blob";
import { ContainerClient } from "@azure/storage-blob";
import { getEnvironmentVariable } from "./utils";
import { context as otContext } from "@opentelemetry/api";

// TODO: validate env vars

const eventHubHandlers: SubscriptionEventHandlers = {
  processEvents: async (events, context) => {
    await otContext.with(otContext.active(), async () => {
      for (const event of events) {
        console.log(`Received event: ${event.body}`);
        await context.updateCheckpoint(event);
      }
    });
  },
  processError: (err) => {
    console.error(err);
    return Promise.reject(err);
  }
};

async function initializeCheckpointStore() {
  const containerClient = new ContainerClient(
    getEnvironmentVariable("STORAGE_CONNECTION_STRING"),
    getEnvironmentVariable("STORAGE_CONTAINER_NAME")
  );
  await containerClient.createIfNotExists();
  return new BlobCheckpointStore(containerClient);
}

export async function initializeEventHub() {
  const checkpointStore = await initializeCheckpointStore();
  const consumer = new EventHubConsumerClient(
    getEnvironmentVariable("EVENTHUB_CONSUMER_GROUP"),
    getEnvironmentVariable("EVENTHUB_CONNECTION_STRING"),
    getEnvironmentVariable("EVENTHUB_NAME"),
    checkpointStore
  );

  const producer = new EventHubProducerClient(
    getEnvironmentVariable("EVENTHUB_CONNECTION_STRING"),
    getEnvironmentVariable("EVENTHUB_NAME")
  );

  consumer.subscribe(eventHubHandlers, {
    // todo: remove this when i can push new events
    startPosition: earliestEventPosition
  });

  return { consumer, producer };
}
