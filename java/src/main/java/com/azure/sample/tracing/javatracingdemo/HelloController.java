package com.azure.sample.tracing.javatracingdemo;


import com.azure.core.models.CloudEvent;
import com.azure.core.models.CloudEventDataFormat;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HelloController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);


    @Autowired
    EventGridPublisherClient<CloudEvent> cloudEventClient;

    @Autowired
    EventGridPublisherClient<EventGridEvent> eventGridClient;

    // cloudEvent sample
    @PostMapping("/messages")
    public ResponseEntity<String> send(@RequestParam("message") String message) throws JsonProcessingException {
        LOGGER.info("Going to send message {}.", message);

        CloudEvent event = new CloudEvent(
                "https://source.example.com",
                "com.Example.ExampleEventType",
                BinaryData.fromObject(Map.of("message", message)),
                CloudEventDataFormat.JSON,
                "application/json");
        cloudEventClient.sendEvent(event);

        return ResponseEntity.ok(message);
    }


    // EventGridSchema sample
    /*@PostMapping("/messages")
    public ResponseEntity<String> send(@RequestParam("message") String message)  {
        LOGGER.info("Going to send message {}.", message);

        String traceparent = "00-" + Span.current().getSpanContext().getTraceId() + "-" + Span.current().getSpanContext().getSpanId() + "-01";
        eventGridClient.sendEvent(new EventGridEvent("test", "com.Example.ExampleEventType", BinaryData.fromObject(Map.of("message", message, "traceparent", traceparent)), "0.1"));

        return ResponseEntity.ok(message);
    }*/
}