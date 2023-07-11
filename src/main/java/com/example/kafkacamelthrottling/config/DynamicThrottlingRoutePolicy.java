package com.example.kafkacamelthrottling.config;

import org.apache.camel.Exchange;
import org.apache.camel.Route;
import org.apache.camel.model.ThrottleDefinition;
import org.apache.camel.support.RoutePolicySupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class DynamicThrottlingRoutePolicy extends RoutePolicySupport {

    @Value("${route.throttling.throttleRate}")
    private int initialThrottleRate;

    private int currentThrottleRate;

    @Override
    public void onExchangeBegin(Route route, Exchange exchange) {

        exchange.getIn().setHeader("throttleRate", currentThrottleRate);
        ThrottleDefinition throttle = new ThrottleDefinition()
                .timePeriodMillis(1000)
                .maximumRequestsPerPeriod(currentThrottleRate);
    }

    public void setCurrentThrottleRate(int currentThrottleRate) {
        this.currentThrottleRate = currentThrottleRate;
    }

    public int getInitialThrottleRate() {
        return initialThrottleRate;
    }

    public int getCurrentThrottleRate() {
        return currentThrottleRate;
    }

    @PostConstruct
    public void setThrottleRate() {
        this.currentThrottleRate = this.initialThrottleRate;
    }
}

