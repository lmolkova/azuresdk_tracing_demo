package com.azure.sample.tracing.javatracingdemo;


import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import reactor.util.context.Context;
import reactor.util.context.ContextView;
import io.opentelemetry.api.trace.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

@Configuration
public class EventProducerConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventProducerConfiguration.class);

   /* @Bean
    public Sinks.Many<Message<String>> many() {
        return Sinks.many().unicast().onBackpressureBuffer();
    }

    @Bean
    public Supplier<Flux<Message<String>>> supply(Sinks.Many<Message<String>> many) {
        return () -> many.asFlux()
                .doOnNext(m -> LOGGER.info("Manually sending message {}", m))
                .contextWrite(Context.of("k", (Object)Span.current()))
                .doOnError(t -> LOGGER.error("Error encountered", t));
    }*/

    @Bean
    public EventHubProducerClient getProducer(Environment env){
        String connectionString = env.getProperty("spring.cloud.azure.eventhub.connection-string");
        String eventHubName = env.getProperty("spring.cloud.azure.eventhub.eventhub-name");
        return new EventHubClientBuilder()
            .connectionString(connectionString, eventHubName)
            .buildProducerClient();
    }
}