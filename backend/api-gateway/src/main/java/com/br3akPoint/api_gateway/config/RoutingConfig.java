package com.br3akPoint.api_gateway.config;

import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class RoutingConfig {

    // ─── Generic Helper ───────────────────────────────────────────────────────

    private RouterFunction<ServerResponse> buildRoute(
            String routeId,
            String gatewayPath,   // e.g. "/v1/api/recipe"
            String replacement,
            String lbServiceName  // e.g. "RECIPE-SERVICE"
    ) {
        return GatewayRouterFunctions.route(routeId)
                .route(
                        RequestPredicates.path(gatewayPath + "/**"),
                        HandlerFunctions.http()
                )
                .before(BeforeFilterFunctions.rewritePath(
                        gatewayPath + "/(?<segment>.*)",
                        replacement + "/${segment}"
                ))
                .filter(LoadBalancerFilterFunctions.lb(lbServiceName))
                .build();
    }

    // ─── Routes ───────────────────────────────────────────────────────────────

    @Bean
    public RouterFunction<ServerResponse> recipeServiceRoute() {
        return buildRoute(
                "recipe-service",
                "/v1/api/recipe",
                "/api/v1",
                "RECIPE-SERVICE"
        );
    }

    @Bean
    public RouterFunction<ServerResponse> authServiceRoute() {
        return buildRoute(
                "auth-service",
                "/v1/api/auth",
                "/auth",
                "AUTH-SERVICE"
        );
    }
}