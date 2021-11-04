package com.azure.sample.tracing.javatracingdemo;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.models.CloudEvent;
import com.azure.spring.integration.core.EventHubHeaders;
import com.azure.spring.integration.core.api.reactor.Checkpointer;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static com.azure.spring.integration.core.AzureHeaders.CHECKPOINTER;

@SpringBootApplication
@EnableScheduling
public class JavaTracingDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(JavaTracingDemoApplication.class, args);
	}

	public static final Logger LOGGER = LoggerFactory.getLogger(JavaTracingDemoApplication.class);
	public static final Tracer tracer = GlobalOpenTelemetry.getTracer("app");
	public static final TextMapPropagator propagator = W3CTraceContextPropagator.getInstance();
	private static final TextMapGetter<CloudEvent> getter = new TextMapGetter<CloudEvent>() {
		@Override
		public Iterable<String> keys(CloudEvent carrier) {
			return Arrays.asList("traceparent", "tracestate");
		}

		@Override
		public String get(CloudEvent carrier, String key) {
			Object value = carrier.getExtensionAttributes().get(key);
			return value == null ? null : value.toString();
		}
	};

	@Bean
	public Consumer<Message<String>> consume() {
		return message -> {
			Checkpointer checkpointer = (Checkpointer) message.getHeaders().get(CHECKPOINTER);

			List<CloudEvent> events = CloudEvent.fromString(message.getPayload());
			SpanBuilder spanBuilder = tracer.spanBuilder("CloudEvents ") // todo what if batch is received?
					.setSpanKind(SpanKind.CONSUMER);

			for (CloudEvent event : events) {
				spanBuilder.addLink(Span.fromContext(propagator.extract(Context.current(), event, getter)).getSpanContext());
			}

			Span span = spanBuilder.startSpan();
			try(Scope s = span.makeCurrent()) {
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
			} finally {
				span.end();
			}

		};
	}

	@Autowired
	QueueClient queueClient;

	@Scheduled(fixedRate=60000)
	public void run() {
		PagedIterable<QueueMessageItem> messages = queueClient.receiveMessages(10, Duration.ofSeconds(10), Duration.ofSeconds(30), com.azure.core.util.Context.NONE);
		messages.forEach(message -> {

			try {
				List<CloudEvent> events = CloudEvent.fromString(message.getBody().toString());
				SpanBuilder spanBuilder = tracer.spanBuilder("CloudEvents Process") // todo what if batch is received?
						.setSpanKind(SpanKind.CONSUMER);

				for (CloudEvent event : events) {
					spanBuilder.addLink(Span.fromContext(propagator.extract(Context.current(), event, getter)).getSpanContext());
				}

				Span span = spanBuilder.startSpan();
				try (Scope s = span.makeCurrent()) {
					LOGGER.info("New message received: '{}", message.getBody().toString());
					queueClient.deleteMessage(message.getMessageId(), message.getPopReceipt());

				} finally {
					span.end();
				}
			} catch (Exception ex) {
				LOGGER.error("Filed to process message", ex);
				queueClient.deleteMessage(message.getMessageId(), message.getPopReceipt());
			}
		});
	}
}