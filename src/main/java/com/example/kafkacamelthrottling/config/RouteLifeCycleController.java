package com.example.kafkacamelthrottling.config;

import org.apache.camel.CamelContext;
import org.springframework.stereotype.Component;

import static com.example.kafkacamelthrottling.constants.ApplicatonConstant.INPUT_TOPIC_CONSUMER_ROUTE_ID;

@Component
public class RouteLifeCycleController {

    private CamelContext camelContext;

    public RouteLifeCycleController(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public void resumeRoute() {
        System.out.println("Starting route: "+ INPUT_TOPIC_CONSUMER_ROUTE_ID);
        try {
            if (!camelContext.getRouteController().getRouteStatus(INPUT_TOPIC_CONSUMER_ROUTE_ID).isStarted()) {
                camelContext.getRouteController().resumeRoute(INPUT_TOPIC_CONSUMER_ROUTE_ID);
            } else {
                System.out.println("Route already running...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void suspendRoute() {
        System.out.println("Stopping route: "+ INPUT_TOPIC_CONSUMER_ROUTE_ID);
        try {
            if (!camelContext.getRouteController().getRouteStatus(INPUT_TOPIC_CONSUMER_ROUTE_ID).isSuspended()) {
                camelContext.getRouteController().suspendRoute(INPUT_TOPIC_CONSUMER_ROUTE_ID);
            } else {
                System.out.println("Route already suspended...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
