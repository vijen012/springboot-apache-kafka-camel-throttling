package com.example.kafkacamelthrottling.routes;

import com.example.kafkacamelthrottling.config.RouteLifeCycleController;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.example.kafkacamelthrottling.constants.RouteLifeCycleEvent.RESUME;
import static com.example.kafkacamelthrottling.constants.RouteLifeCycleEvent.SUSPEND;

@Component
public class CamelRouteLifeCycleControllerRoute extends RouteBuilder {

    private RouteLifeCycleController routeLifeCycleController;

    @Value("${camel.kafka.routes.camelRouteLifeCycleControllerRoute.url}")
    private String camelRouteLifeCycleControllerRouteURI;

    public CamelRouteLifeCycleControllerRoute(RouteLifeCycleController routeLifeCycleController) {
        this.routeLifeCycleController = routeLifeCycleController;
    }

    @Override
    public void configure() throws Exception {

       CamelContext context = this.getCamelContext();

        // Route to RESUME or SUSPEND another route
        from(camelRouteLifeCycleControllerRouteURI)
                .routeId("camel-route-lifecycle-controller-route")
                .process(exchange ->  {
                    //Fetch all the routes Id
                    //context.getRoutes().forEach(route -> System.out.println("RouteId: "+ route.getRouteId()));
                    // Extract the new throttle rate from the exchange
                    String routeControlEvent = exchange.getIn().getBody().toString();
                    System.out.println("Event received: "+ routeControlEvent);
                    if (RESUME.name().equalsIgnoreCase(routeControlEvent)) {
                        routeLifeCycleController.resumeRoute();
                    } else if (SUSPEND.name().equalsIgnoreCase(routeControlEvent)) {
                        routeLifeCycleController.suspendRoute();
                    } else {
                        System.out.println("Received unknown lifecycle event: "+ routeControlEvent);
                    }
                });
    }
}
