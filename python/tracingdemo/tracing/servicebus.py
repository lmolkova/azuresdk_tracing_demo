import os

# Regular open telemetry usage from here, see https://github.com/open-telemetry/opentelemetry-python
# for details
from azure.servicebus import ServiceBusClient, ServiceBusMessage

import os

connection_str = os.environ['SERVICE_BUS_CONNECTION_STRING']
queue_name = os.environ['SERVICE_BUS_QUEUE_NAME']

def send_messages(message):
    with ServiceBusClient.from_connection_string(connection_str) as client:
        with client.get_queue_sender(queue_name) as sender:
            # Sending a list of messages
            sender.send_messages(ServiceBusMessage(message))
            return "Message successfully sent. Content: {}".format(message)
