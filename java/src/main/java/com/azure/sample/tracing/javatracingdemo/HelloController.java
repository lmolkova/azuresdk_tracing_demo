package com.azure.sample.tracing.javatracingdemo;


import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    EventHubProducerClient producerClient;

    //@Autowired
    //private Sinks.Many<Message<String>> many;

    /**
     * Posts a message to a Azure Event Hub
     */
    @PostMapping("/messages")
    public ResponseEntity<String> send(@RequestParam("message") String message) {
        LOGGER.info("Going to send message {}.", message);

        EventDataBatch eventDataBatch = producerClient.createBatch();

        if (eventDataBatch.tryAdd(new EventData(message))) {
            producerClient.send(eventDataBatch);
        }

        return ResponseEntity.ok(message);
    }
}