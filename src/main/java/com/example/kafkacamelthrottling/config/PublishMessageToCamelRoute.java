package com.example.kafkacamelthrottling.config;

import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PublishMessageToCamelRoute {

    private final CamelContext camelContext;

    @Autowired
    public PublishMessageToCamelRoute(CamelContext camelContext) {
        this.camelContext = camelContext;
    }

    public void sendMessageToCamelRoute(String message) {
        camelContext.createProducerTemplate().sendBody("direct:produceToKafka", message);
    }
}
