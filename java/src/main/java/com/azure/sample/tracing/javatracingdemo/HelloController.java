package com.azure.sample.tracing.javatracingdemo;


import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.blob.BlobClientBuilder;

import com.azure.storage.queue.QueueClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
public class HelloController {

    private static final ObjectMapper SERIALIZER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);
    public static final Tracer tracer = GlobalOpenTelemetry.getTracer("app");
    public static final TextMapPropagator propagator = W3CTraceContextPropagator.getInstance();
    private static final TextMapSetter<CloudEvent> SETTER = new TextMapSetter<CloudEvent>() {

        @Override
        public void set(CloudEvent carrier, String key, String value) {
            carrier.addExtensionAttribute(key, value);
        }
    };

    @Autowired
    EventHubProducerClient producerClient;

    @Autowired
    EventGridPublisherClient<CloudEvent> cloudEventClient;

    @Autowired
    QueueClient queueClient;

    /**
     * Posts a message to a Azure Event Hub
     */
    @PostMapping("/messages")
    public ResponseEntity<String> send(@RequestParam("message") String message) throws JsonProcessingException {
        LOGGER.info("Going to send message {}.", message);

        /*EventDataBatch eventDataBatch = producerClient.createBatch();

        if (eventDataBatch.tryAdd(new EventData(message))) {
            producerClient.send(eventDataBatch);
        }*/

        List<CloudEvent> events = new ArrayList<>();

        CloudEvent event = new CloudEvent(
                "https://source.example.com",
                "com.Example.ExampleEventType",
                BinaryData.fromObject(Map.of("message", message)),
                CloudEventDataFormat.JSON,
                "application/json");


        Span span = tracer.spanBuilder("CloudEvents Create " + event.getType())
                .setSpanKind(SpanKind.PRODUCER)
                .startSpan();
        propagator.inject(Context.current().with(span), event, SETTER);
        span.end();

        events.add(event);
        cloudEventClient.sendEvents(events);
        //queueClient.sendMessage(SERIALIZER.writeValueAsString(events));

        return ResponseEntity.ok(message);
    }
}