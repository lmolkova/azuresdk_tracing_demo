package com.azure.sample.tracing.javatracingdemo;


import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.CloudEvent;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class EventProducerConfiguration {

    @Bean
    EventGridPublisherClient<CloudEvent> getEventGridClient(Environment env) {
        return  new EventGridPublisherClientBuilder()
                .endpoint(env.getProperty("spring.cloud.azure.eventgrid.endpoint"))
                .credential(new AzureKeyCredential(env.getProperty("spring.cloud.azure.eventgrid.key")))
                .buildCloudEventPublisherClient();
    }

    @Bean
    EventGridPublisherClient<EventGridEvent> getEventGridEGSchemaClient(Environment env) {
        return  new EventGridPublisherClientBuilder()
                .endpoint(env.getProperty("spring.cloud.azure.eventgrid.endpoint"))
                .credential(new AzureKeyCredential(env.getProperty("spring.cloud.azure.eventgrid.key")))
                .buildEventGridEventPublisherClient();
    }
}