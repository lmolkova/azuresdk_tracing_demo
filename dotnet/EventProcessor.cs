using Azure.Messaging.EventHubs;
using Azure.Messaging.EventHubs.Consumer;
using Azure.Messaging.EventHubs.Processor;
using Azure.Storage.Blobs;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace dotnet
{
    class EventProcessor : IHostedService, IDisposable
    {
        private readonly ILogger<EventProcessor> logger;
        private readonly EventProcessorClient processor;
        private readonly BlobContainerClient containterClient;

        public EventProcessor(IOptions<EventHubConfigurationOptions> eventHubOptions, IOptions<StorageConfigurationOptions> storageOptions, ILogger<EventProcessor> logger)
        {
            this.containterClient = new BlobContainerClient(storageOptions.Value.ConnectionString, storageOptions.Value.BlobContainerName);
            this.processor = new EventProcessorClient(containterClient, EventHubConsumerClient.DefaultConsumerGroupName, eventHubOptions.Value.ConnectionString, eventHubOptions.Value.EventHubName);
            this.logger = logger;
        }

        public void Dispose()
        {
            StopAsync(CancellationToken.None).GetAwaiter().GetResult();
        }

        public Task StartAsync(CancellationToken cancellationToken)
        {

            this.processor.ProcessEventAsync += ProcessEventHandler;
            this.processor.ProcessErrorAsync += ProcessErrorHandler;

            return processor.StartProcessingAsync();
        }

        public async Task StopAsync(CancellationToken cancellationToken)
        {
            try
            {
                await processor.StopProcessingAsync();
            }
            finally
            {
                // To prevent leaks, the handlers should be removed when processing is complete.
                processor.ProcessEventAsync -= ProcessEventHandler;
                processor.ProcessErrorAsync -= ProcessErrorHandler;
            }
        }

        private async Task ProcessEventHandler(ProcessEventArgs eventArgs)
        {
            if (eventArgs.HasEvent) {
                logger.LogInformation("Received message {message} from partition {partition}", eventArgs.Data.EventBody.ToString(), eventArgs.Partition);
            }

            await eventArgs.UpdateCheckpointAsync();
        }

        private Task ProcessErrorHandler(ProcessErrorEventArgs eventArgs)
        {
            logger.LogError(eventArgs.Exception, "Received exception for {operation} from partition {partition}", eventArgs.Operation, eventArgs.PartitionId);
            return Task.CompletedTask;
        }
    }
}
