from azure.core.settings import settings
from azure.core.tracing.ext.opentelemetry_span import OpenTelemetrySpan

settings.tracing_implementation = OpenTelemetrySpan

# In the below example, we use a simple console exporter, uncomment these lines to use
# the Azure Monitor Exporter. It can be installed from https://pypi.org/project/opentelemetry-azure-monitor/
# Example of Azure Monitor exporter, but you can use anything OpenTelemetry supports
# from azure_monitor import AzureMonitorSpanExporter
# exporter = AzureMonitorSpanExporter(
#     instrumentation_key="uuid of the instrumentation key (see your Azure Monitor account)"
# )

# Regular open telemetry usage from here, see https://github.com/open-telemetry/opentelemetry-python
# for details
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import ConsoleSpanExporter
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from azure.monitor.opentelemetry.exporter import AzureMonitorTraceExporter

# Simple console exporter
exporter = ConsoleSpanExporter()

trace.set_tracer_provider(TracerProvider())
tracer = trace.get_tracer(__name__)

span_processor = BatchSpanProcessor(
    AzureMonitorTraceExporter.from_connection_string(
        os.environ["APPLICATIONINSIGHTS_CONNECTION_STRING"]
    )
)
trace.get_tracer_provider().add_span_processor(span_processor)
from azure.eventhub import EventHubConsumerClient
from azure.eventhub.extensions.checkpointstoreblob import BlobCheckpointStore
import os

connection_str = os.environ['EVENTHUB_CONNECTION_STRING']
consumer_group = os.environ['EVENTHUB_CONSUMER_GROUP']
eventhub_name = os.environ['EVENTHUB_NAME']
storage_connection_str = os.environ['CHECKPOINT_STORAGE_CONNECTION_STRING']
container_name = os.environ['CHECKPOINT_CONTAINER_NAME']

def on_event(partition_context, event):
    partition_context.update_checkpoint(event)  # Or update_checkpoint every N events for better performance.

def receive_messages():
    with tracer.start_as_current_span(name="MyApplication"):
        checkpoint_store = BlobCheckpointStore.from_connection_string(
            storage_connection_str,
            container_name
        )
        client = EventHubConsumerClient.from_connection_string(
            connection_str,
            consumer_group,
            eventhub_name=eventhub_name,
            checkpoint_store=checkpoint_store,
        )
        with client:
            client.receive(on_event)
