#!/usr/bin/env python
"""Django's command-line utility for administrative tasks."""
import os
import sys
import asyncio

from azure.core.settings import settings
from azure.core.tracing.ext.opentelemetry_span import OpenTelemetrySpan

settings.tracing_implementation = OpenTelemetrySpan


from opentelemetry import trace
from opentelemetry.sdk.trace import TracerProvider
from opentelemetry.sdk.trace.export import BatchSpanProcessor
from azure.monitor.opentelemetry.exporter import AzureMonitorTraceExporter
from opentelemetry.instrumentation.django import DjangoInstrumentor
from opentelemetry.instrumentation.urllib3 import URLLib3Instrumentor
from opentelemetry.instrumentation.requests import RequestsInstrumentor

from azure.eventhub import EventHubConsumerClient
from azure.eventhub.extensions.checkpointstoreblob import BlobCheckpointStore

connection_str = os.environ['EVENTHUB_CONNECTION_STRING']
consumer_group = os.environ['EVENTHUB_CONSUMER_GROUP']
eventhub_name = os.environ['EVENTHUB_NAME']
storage_connection_str = os.environ['CHECKPOINT_STORAGE_CONNECTION_STRING']
container_name = os.environ['CHECKPOINT_CONTAINER_NAME']

async  def on_event(partition_context, event):
    print(event)
    await partition_context.update_checkpoint(event)  # Or update_checkpoint every N events for better performance.


async def receive_messages():
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
    async with client:
        await client.receive(on_event)

async def main():
    """Run administrative tasks."""
    os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'tracingdemo.settings')
    try:
        from django.core.management import execute_from_command_line
    except ImportError as exc:
        raise ImportError(
            "Couldn't import Django. Are you sure it's installed and "
            "available on your PYTHONPATH environment variable? Did you "
            "forget to activate a virtual environment?"
        ) from exc

    DjangoInstrumentor().instrument()
    RequestsInstrumentor().instrument()
    URLLib3Instrumentor().instrument()

    trace.set_tracer_provider(TracerProvider())
    tracer = trace.get_tracer(__name__)

    exporter = AzureMonitorTraceExporter.from_connection_string(os.environ["APPLICATIONINSIGHTS_CONNECTION_STRING"])
    span_processor = BatchSpanProcessor(exporter)
    trace.get_tracer_provider().add_span_processor(span_processor)
    execute_from_command_line(sys.argv)
    await receive_messages()



if __name__ == '__main__':
    loop = asyncio.get_event_loop()
    loop.run_until_complete(main())
