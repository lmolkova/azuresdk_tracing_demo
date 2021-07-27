package com.azure.sample.tracing.javatracingdemo;

import com.azure.core.models.CloudEvent;
import com.azure.spring.integration.core.EventHubHeaders;
import com.azure.spring.integration.core.api.reactor.Checkpointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.Message;

import java.util.List;
import java.util.function.Consumer;

import static com.azure.spring.integration.core.AzureHeaders.CHECKPOINTER;

@SpringBootApplication
public class JavaTracingDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(JavaTracingDemoApplication.class, args);
	}

	public static final Logger LOGGER = LoggerFactory.getLogger(JavaTracingDemoApplication.class);

	//@Bean
	public Consumer<Message<String>> consume() {
		return message -> {
			Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);

			LOGGER.info("New message received: '{}', partition key: {}, sequence number: {}, offset: {}, enqueued time: {}",
					message.getPayload(),
					message.getHeaders().get(EventHubHeaders.PARTITION_KEY),
					message.getHeaders().get(EventHubHeaders.SEQUENCE_NUMBER),
					message.getHeaders().get(EventHubHeaders.OFFSET),
					message.getHeaders().get(EventHubHeaders.ENQUEUED_TIME));


			checkpointer.success()
					.doOnSuccess(success -> LOGGER.info("Message '{}' successfully checkpointed", message.getPayload()))
					.doOnError(error -> LOGGER.error("Exception found", error))
					.subscribe();
		};
	}
}