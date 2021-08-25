import {
  EventHubConsumerClient,
  EventHubProducerClient,
  SubscriptionEventHandlers
} from "@azure/event-hubs";
import { BlobCheckpointStore } from "@azure/eventhubs-checkpointstore-blob";
import { ContainerClient } from "@azure/storage-blob";
import { getEnvironmentVariable } from "./utils";

// TODO: work out the naming conventions of these spans.
const eventHubHandlers: SubscriptionEventHandlers = {
  processEvents: async (events, context) => {
    if (events.length === 0) {
      return;
    }

    for (const event of events) {
      console.log(`Received event: ${JSON.stringify(event.body)}`);
    }
    await context.updateCheckpoint(events[events.length - 1]);
  },
  processError: (err) => {
    return Promise.reject(err);
  }
};

export function initializeEventHub() {
  const containerClient = new ContainerClient(
    getEnvironmentVariable("CHECKPOINT_STORAGE_CONNECTION_STRING"),
    getEnvironmentVariable("CHECKPOINT_CONTAINER_NAME")
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
