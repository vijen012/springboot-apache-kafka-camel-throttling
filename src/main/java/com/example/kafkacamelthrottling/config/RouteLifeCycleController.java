package com.example.kafkacamelthrottling.config;

import org.apache.camel.CamelContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

import static com.example.kafkacamelthrottling.constants.ApplicatonConstant.INPUT_TOPIC_CONSUMER_ROUTE_ID;

@Component
public class RouteLifeCycleController {

    private CamelContext camelContext;

    public RouteLifeCycleController(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public void startRouteAsync() {
        System.out.println("Starting route: "+ INPUT_TOPIC_CONSUMER_ROUTE_ID);
        CompletableFuture.runAsync(() -> {
            try {
                camelContext.getRouteController().startRoute(INPUT_TOPIC_CONSUMER_ROUTE_ID);
                System.out.println("Route started..."+ INPUT_TOPIC_CONSUMER_ROUTE_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void stopRouteAsync() {
        System.out.println("Stopping route: "+ INPUT_TOPIC_CONSUMER_ROUTE_ID);
        CompletableFuture.runAsync(() -> {
            try {
                camelContext.getRouteController().stopRoute(INPUT_TOPIC_CONSUMER_ROUTE_ID);
                System.out.println("Route stopped..."+ INPUT_TOPIC_CONSUMER_ROUTE_ID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
