package com.example.kafkacamelthrottling.config;

import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.model.ThrottleDefinition;
import org.apache.camel.support.RoutePolicySupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DynamicThrottlingRoutePolicy extends RoutePolicySupport {

    @Value("${route.throttling.throttleRate}")
    private int throttleRate;

    @Override
    public void onExchangeBegin(Route route, Exchange exchange) {

        exchange.getIn().setHeader("throttleRate", throttleRate);
        ThrottleDefinition throttle = new ThrottleDefinition()
                .timePeriodMillis(1000)
                .maximumRequestsPerPeriod(throttleRate);
    }

    public void setThrottleRate(int throttleRate) {
        System.out.println(String.format("Updating throttleRate from %s to %s", this.throttleRate, throttleRate));
        this.throttleRate = throttleRate;
    }
}

