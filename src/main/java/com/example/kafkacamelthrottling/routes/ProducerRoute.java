package com.example.kafkacamelthrottling.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import static com.example.kafkacamelthrottling.constants.ApplicatonConstant.INPUT_TOPIC_PRODUCER_ROUTE_ID;

@Component
public class ProducerRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        // Kafka Producer
        from("direct:produceToKafka")
                .routeId(INPUT_TOPIC_PRODUCER_ROUTE_ID)
                .log("Message received from Kafka : ${body}")
                .to("kafka:input?brokers=localhost:9092");
    }
}
