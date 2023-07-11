package com.example.kafkacamelthrottling.routes;

import com.example.kafkacamelthrottling.config.DynamicThrottlingRoutePolicy;
import com.example.kafkacamelthrottling.config.MessageProcessingEventConfigData;
import com.example.kafkacamelthrottling.config.RouteLifeCycleController;
import com.example.kafkacamelthrottling.constants.MessageProcessingControlEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ControlEventMessageProcessingRoute extends RouteBuilder {

    @Value("${camel.kafka.routes.messageProcessingControlEventRoute.url}")
    private String messageProcessingControlEventRouteURI;

    private DynamicThrottlingRoutePolicy dynamicThrottlingRoutePolicy;

    private RouteLifeCycleController routeLifeCycleController;

    public ControlEventMessageProcessingRoute(DynamicThrottlingRoutePolicy dynamicThrottlingRoutePolicy,
                                              RouteLifeCycleController routeLifeCycleController) {
        this.dynamicThrottlingRoutePolicy = dynamicThrottlingRoutePolicy;
        this.routeLifeCycleController = routeLifeCycleController;
    }

    @Override
    public void configure() throws Exception {

       CamelContext context = this.getCamelContext();

        // Route to RESUME or SUSPEND another route
        from(messageProcessingControlEventRouteURI)
                .routeId("camel-route-lifecycle-controller-route")
                .process(exchange ->  {
                    //Fetch all the routes Id
                    context.getRoutes().forEach(route -> {
                        System.out.println("RouteId: "+ route.getRouteId());

                    });

                    // Extract the new throttle rate from the exchange
                    String messageProcessingEventConfigJson = exchange.getIn().getBody().toString();
                    System.out.println("Event received: "+ messageProcessingEventConfigJson);
                    MessageProcessingEventConfigData messageProcessingEventConfigData = new ObjectMapper()
                            .readValue(messageProcessingEventConfigJson, MessageProcessingEventConfigData.class);
                    processMessageControlEvent(messageProcessingEventConfigData);
                });
    }

    private void processMessageControlEvent(MessageProcessingEventConfigData messageProcessingEventConfigData) {
        String messageProcessControlEvent = messageProcessingEventConfigData.getMessageProcessingControlEvent().name();
        if (MessageProcessingControlEvent.START.name().equalsIgnoreCase(messageProcessControlEvent)) {
            routeLifeCycleController.resumeRoute();
        } else if (MessageProcessingControlEvent.STOP.name().equalsIgnoreCase(messageProcessControlEvent)) {
            routeLifeCycleController.suspendRoute();
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
}
