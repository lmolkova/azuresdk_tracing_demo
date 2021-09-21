using Azure.Messaging.EventHubs;
using Azure.Messaging.EventHubs.Consumer;
using Azure.Messaging.EventHubs.Processor;
using Azure.Messaging.EventHubs.Producer;
using Azure.Storage.Blobs;
using Microsoft.Extensions.Azure;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using System;
using System.Diagnostics;
using System.Threading;
using System.Threading.Tasks;

namespace dotnet
{
    class EventProcessor : IHostedService, IDisposable
    {
        private readonly ILogger<EventProcessor> logger;
        private readonly EventProcessorClient processor;
        private readonly BlobContainerClient containterClient;

        public EventProcessor(BlobServiceClient blobServiceClient, ILogger<EventProcessor> logger, IConfiguration configuration)
        {
            this.containterClient = blobServiceClient.GetBlobContainerClient(configuration.GetSection("Storage").GetValue<string>("BlobContainerName"));
            this.processor = new EventProcessorClient(
                containterClient,
                EventHubConsumerClient.DefaultConsumerGroupName,
                configuration["EventHub:ConnectionString"],
                configuration["EventHub:EventHubName"]);

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
                eventArgs.Data.Properties.TryGetValue("diagnostic-id", out var diagnsosticId);
                eventArgs.Data.Properties.TryGetValue("Diagnostic-Id", out var DiagnsosticId);
                logger.LogInformation("Received message {message} from partition {partition}, current activity {}, diag-id {}, Diag0Id {}", eventArgs.Data.EventBody.ToString(), eventArgs.Partition, Activity.Current?.Id,
                    diagnsosticId, DiagnsosticId);

                //var cc = this.containterClient.GetBlobClient("123");
                //var props = await cc.GetPropertiesAsync();
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
