package com.example.kafkacamelthrottling.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CamelKafkaProcessorRoute extends RouteBuilder {

    @Value("${route.throttling.throttleRate}")
    private int throttleRate;

    @Override
    public void configure() throws Exception {

        from("kafka:input?brokers=localhost:9092&autoOffsetReset=earliest")
                .routeId("input-topic-consume-route")
                .process(exchange -> exchange.getIn().setHeader("throttleRate", throttleRate))
                .log("on the topic ${headers[throttleRate]}")
                .throttle().expression(header("throttleRate")).timePeriodMillis(10000)
                .log("Message received from Kafka: ${body}");
/*                .log("On the topic ${headers[kafka.TOPIC]}")
                .log("on the partition ${headers[kafka.PARTITION]}")
                .log("with the offset ${headers[kafka.OFFSET]}")
                .log("with the key ${headers[kafka.KEY]}");*/


        // Route to change the throttling rate
        from("kafka:rate-limit?brokers=localhost:9092")
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
