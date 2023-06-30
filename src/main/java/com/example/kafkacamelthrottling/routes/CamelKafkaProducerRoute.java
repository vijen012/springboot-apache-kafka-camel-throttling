package com.example.kafkacamelthrottling.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class CamelKafkaProducerRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        // Kafka Producer
        from("direct:produceToKafka")
                .routeId("input-topic-produce-route")
                .log("Message received from Kafka : ${body}")
                .to("kafka:input?brokers=localhost:9092");
    }
}
