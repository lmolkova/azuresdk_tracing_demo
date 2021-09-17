import os
from azure.core.settings import settings
from azure.core.tracing.ext.opentelemetry_span import OpenTelemetrySpan

settings.tracing_implementation = OpenTelemetrySpan

# Regular open telemetry usage from here, see https://github.com/open-telemetry/opentelemetry-python
# for details
from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import ConsoleSpanExporter
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from azure.monitor.opentelemetry.exporter import AzureMonitorTraceExporter
from azure.servicebus import ServiceBusClient, ServiceBusMessage
import os

# Simple console exporter
exporter = ConsoleSpanExporter()

trace.set_tracer_provider(TracerProvider())
tracer = trace.get_tracer(__name__)

span_processor = BatchSpanProcessor(
    AzureMonitorTraceExporter()
)
trace.get_tracer_provider().add_span_processor(span_processor)

connection_str = os.environ['SERVICE_BUS_CONN_STR']
queue_name = os.environ['SERVICE_BUS_QUEUE_NAME']

def send_messages(message):
    with tracer.start_as_current_span(name="MyApplication"):
        with ServiceBusClient.from_connection_string(connection_str) as client:
            with client.get_queue_sender(queue_name) as sender:
                # Sending a list of messages
                sender.send_messages(ServiceBusMessage(message))
                return "Message successfully sent. Content: {}".format(message)
