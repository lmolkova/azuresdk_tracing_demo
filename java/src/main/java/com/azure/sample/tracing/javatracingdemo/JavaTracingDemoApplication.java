package com.azure.sample.tracing.javatracingdemo;

import com.azure.spring.integration.core.EventHubHeaders;
import com.azure.spring.integration.core.api.reactor.Checkpointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.function.Consumer;

import static com.azure.spring.integration.core.AzureHeaders.CHECKPOINTER;

@SpringBootApplication
@EnableScheduling
public class JavaTracingDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(JavaTracingDemoApplication.class, args);
	}

	public static final Logger LOGGER = LoggerFactory.getLogger(JavaTracingDemoApplication.class);

	@Bean
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