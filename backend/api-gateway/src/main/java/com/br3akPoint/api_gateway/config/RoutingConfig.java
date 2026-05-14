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
                        RequestPredicates.path(gatewayPath + "/**")
                                .or(RequestPredicates.path(gatewayPath)),
                        HandlerFunctions.http()
                )
                .before(BeforeFilterFunctions.rewritePath(
                        gatewayPath + "(?<segment>/.*)?",
                        replacement + "${segment}"
                ))
                // --- HEADER FORWARDING LOGIC START ---
                .before(request -> {
                    var auth = SecurityContextHolder.getContext().getAuthentication();

                    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserRequestData userRequest) {
                        if (userRequest.getUserId() != null) {
                            var requestBuilder =  ServerRequest.from(request)
                                    .header("X-User-Id", String.valueOf(userRequest.getUserId()));

                            if(userRequest.getEmail() != null) {
                                requestBuilder.header("X-User-Email", userRequest.getEmail());
                            }

                            if(userRequest.getDeviceType() != null) {
                                requestBuilder.header("X-Device-Type", userRequest.getDeviceType());
                            }

                            if(userRequest.getDeviceId() != null) {
                                requestBuilder.header("X-Device-Id", userRequest.getDeviceId());
                            }

                            return requestBuilder.build();
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
                "/recipe",
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

    @Bean
    public RouterFunction<ServerResponse> storageServiceRoute() {
        return buildRoute(
                "storage-service",
                "/v1/api/storage",
                "/storage",
                "STORAGE-SERVICE"
        );
    }

    @Bean
    public RouterFunction<ServerResponse> notificationServiceRoute() {
        return buildRoute(
                "notification-service",
                "/v1/api/user/fcm",
                "/notification/fcm",
                "NOTIFICATION-SERVICE"
        );
    }
}