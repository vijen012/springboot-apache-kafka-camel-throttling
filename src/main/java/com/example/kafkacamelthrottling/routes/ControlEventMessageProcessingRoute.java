package com.example.kafkacamelthrottling.routes;

import com.example.kafkacamelthrottling.config.DynamicThrottlingRoutePolicy;
import com.example.kafkacamelthrottling.config.MessageProcessingEventConfigData;
import com.example.kafkacamelthrottling.config.RouteLifeCycleController;
import com.example.kafkacamelthrottling.constants.MessageProcessingControlEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class ControlEventMessageProcessingRoute extends RouteBuilder {

    @Value("${camel.kafka.routes.messageProcessingControlEventRoute.url}")
    private String messageProcessingControlEventRouteURI;

    @Value("${camel.kafka.routes.messageProcessingControlEventRoute.groupId}")
    private String groupId;

    private DynamicThrottlingRoutePolicy dynamicThrottlingRoutePolicy;

    private RouteLifeCycleController routeLifeCycleController;


    public ControlEventMessageProcessingRoute(DynamicThrottlingRoutePolicy dynamicThrottlingRoutePolicy,
                                              RouteLifeCycleController routeLifeCycleController) {
        this.dynamicThrottlingRoutePolicy = dynamicThrottlingRoutePolicy;
        this.routeLifeCycleController = routeLifeCycleController;
    }

    @Override
    public void configure() throws Exception {
        var context = this.getCamelContext();
        //Setting offset to endOffset-1 to read the last message instead of reading old events as we need to act
        // on latest event only when application starts
        resetOffset(context);

        // Route to RESUME or SUSPEND another route
        fromF(messageProcessingControlEventRouteURI)
                .routeId("camel-route-lifecycle-controller-route")
                .process(exchange ->  {
                    System.out.println("camel-route-lifecycle-controller-route-started");
                    //Fetch all the routes Id
                    context.getRoutes().forEach(route -> System.out.println("RouteId: "+ route.getRouteId()));
                    // Extract the new throttle rate from the exchange
                    var messageProcessingEventConfigJson = exchange.getIn().getBody().toString();
                    System.out.println("Event received: "+ messageProcessingEventConfigJson);
                    var messageProcessingEventConfigData = new ObjectMapper()
                            .readValue(messageProcessingEventConfigJson, MessageProcessingEventConfigData.class);
                    processMessageControlEvent(messageProcessingEventConfigData);
                });
    }

    private void processMessageControlEvent(MessageProcessingEventConfigData messageProcessingEventConfigData) {
        var messageProcessControlEvent = messageProcessingEventConfigData.getMessageProcessingControlEvent().name();
        if (MessageProcessingControlEvent.START.name().equalsIgnoreCase(messageProcessControlEvent)) {
            routeLifeCycleController.startRouteAsync();
        } else if (MessageProcessingControlEvent.STOP.name().equalsIgnoreCase(messageProcessControlEvent)) {
            routeLifeCycleController.stopRouteAsync();
        } else if (MessageProcessingControlEvent.UPDATE_THROTTLE.name().equalsIgnoreCase(messageProcessControlEvent)) {
            System.out.println(String.format("Updating throttle-rate from %s to %s",
                    dynamicThrottlingRoutePolicy.getCurrentThrottleRate(), messageProcessingEventConfigData.getThrottleRate()));
            dynamicThrottlingRoutePolicy.setCurrentThrottleRate(messageProcessingEventConfigData.getThrottleRate());
        } else if (MessageProcessingControlEvent.REVERT_THROTTLE.name().equalsIgnoreCase(messageProcessControlEvent)) {
            System.out.println(String.format("Reverting throttle-rate to initial value: %s",
                    dynamicThrottlingRoutePolicy.getInitialThrottleRate()));
            dynamicThrottlingRoutePolicy.setCurrentThrottleRate(dynamicThrottlingRoutePolicy.getInitialThrottleRate());
        } else {
            System.out.println("Unknown event received: "+ messageProcessControlEvent + ", there is a no action defined for this event");
        }
    }

    private void resetOffset(CamelContext camelContext) {
        var kafkaEndpoint = (KafkaEndpoint) camelContext.getEndpoint(messageProcessingControlEventRouteURI);
        var consumerProperties = kafkaEndpoint.getConfiguration().createConsumerProperties();
        consumerProperties.put("group.id", kafkaEndpoint.getConfiguration().getGroupId());
        consumerProperties.put("bootstrap.servers", kafkaEndpoint.getConfiguration().getBrokers());
        try (KafkaConsumer<String, String> kafkaConsumer = (KafkaConsumer<String, String>) kafkaEndpoint.getKafkaClientFactory()
                .getConsumer(consumerProperties)) {
            var topicPartition = new TopicPartition(kafkaEndpoint.getConfiguration().getTopic(), 0);
            var endOffset = getEndOffset(kafkaConsumer, topicPartition);
            System.out.println("endOffset----> "+ endOffset);
            var seekOffset = endOffset == 0 ? endOffset : endOffset - 1;
            setOffsetToSeek(kafkaConsumer, topicPartition, seekOffset);
        }
    }

     // Helper method to get the end offset for a topic partition
    private long getEndOffset(KafkaConsumer<String, String> kafkaConsumer, TopicPartition topicPartition) {
        kafkaConsumer.assign(Collections.singletonList(topicPartition));
        kafkaConsumer.seekToEnd(Collections.singleton(topicPartition));
        return kafkaConsumer.position(topicPartition);
    }

    // Helper method to set the offset to seek
    private void setOffsetToSeek(KafkaConsumer<String, String> kafkaConsumer, TopicPartition topicPartition, long seekOffset) {
        kafkaConsumer.assign(Collections.singletonList(topicPartition));
        kafkaConsumer.seek(topicPartition, seekOffset);
    }
}
