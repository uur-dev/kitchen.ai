package com.br3akPoint.api_gateway.config;

import com.br3akPoint.api_gateway.data.UserRequestData;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
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
                // --- HEADER FORWARDING LOGIC START ---
                .before(request -> {
                    // SecurityContext se validated user details uthayen
                    var auth = SecurityContextHolder.getContext().getAuthentication();

                    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserRequestData userRequest) {
                        if (userRequest.getUserId() != null) {
                            return ServerRequest.from(request)
                                    .header("X-User-Id", String.valueOf(userRequest.getUserId()))
                                    .header("X-User-Email", userRequest.getEmail())
                                    .build();
                        }
                    }
                    return request;
                })
                // --- HEADER FORWARDING LOGIC END ---
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