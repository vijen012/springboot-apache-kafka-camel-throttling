package com.example.kafkacamelthrottling.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.example.kafkacamelthrottling.constants.ApplicatonConstant.INPUT_TOPIC_CONSUMER_ROUTE_ID;

@Component
public class ProcessorRoute extends RouteBuilder {

    @Value("${camel.kafka.routes.inputRoute.url}")
    private String inputRouteURI;

    @Value("${camel.kafka.routes.rateLimiterRoute.url}")
    private String rateLimiterRouteURI;

    @Value("${route.throttling.throttleRate}")
    private int throttleRate;

    @Value("${route.throttling.timePeriodMillis}")
    private int timePeriodMillis;

    @Override
    public void configure() throws Exception {

        from(inputRouteURI)
                .routeId(INPUT_TOPIC_CONSUMER_ROUTE_ID)
                .process(exchange -> exchange.getIn().setHeader("throttleRate", throttleRate))
                .log("on the topic ${headers[throttleRate]}")
                .throttle().expression(header("throttleRate")).timePeriodMillis(timePeriodMillis)
                .log("Message received from Kafka: ${body}");
/*                .log("On the topic ${headers[kafka.TOPIC]}")
                .log("on the partition ${headers[kafka.PARTITION]}")
                .log("with the offset ${headers[kafka.OFFSET]}")
                .log("with the key ${headers[kafka.KEY]}");*/


        // Route to change the throttling rate
        from(rateLimiterRouteURI)
                .routeId("change-throttle-route")
                .process(exchange ->  {
                    // Extract the new throttle rate from the exchange
                    int newRate = Integer.parseInt(exchange.getIn().getBody().toString());
                    System.out.println("rate limit received: "+ newRate);
                    // Update the throttleRate variable
                    throttleRate = newRate;
                    System.out.println("rate limit updated: "+ throttleRate);
                });


        // ControlBus route to change the throttling rate and restart the route
/*        from("kafka:rate-limit?brokers=localhost:9092")
                .routeId("change-throttle-route")
                .process(exchange ->  {
                    // Extract the new throttle rate from the exchange
                    int newRate = Integer.parseInt(exchange.getIn().getBody().toString());
                    System.out.println("rate limit received: "+ newRate);
                    // Update the throttleRate variable
                    throttleRate = newRate;
                    System.out.println("rate limit updated: "+ throttleRate);
                }).to("controlbus:route?routeId=input-topic-consume-route&action=restart");*/
    }
}
