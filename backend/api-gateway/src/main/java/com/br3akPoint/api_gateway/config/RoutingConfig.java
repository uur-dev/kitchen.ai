package com.br3akPoint.api_gateway.config;

import com.br3akPoint.api_gateway.data.UserRequestData;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.time.Duration;

import static org.springframework.cloud.gateway.server.mvc.filter.Bucket4jFilterFunctions.rateLimit;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

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
                        http()
                )
                .before(BeforeFilterFunctions.rewritePath(
                        gatewayPath + "(?<segment>/.*)?",
                        replacement + "${segment}"
                ))
                // --- HEADER FORWARDING ---
                .before(request -> {
                    var auth = SecurityContextHolder.getContext().getAuthentication();

                    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserRequestData userRequest) {
                        if (userRequest.getUserId() != null) {
                            var requestBuilder = ServerRequest.from(request)
                                    .header("X-User-Id", String.valueOf(userRequest.getUserId()));

                            if (userRequest.getEmail() != null) {
                                requestBuilder.header("X-User-Email", userRequest.getEmail());
                            }

                            if (userRequest.getDeviceType() != null) {
                                requestBuilder.header("X-Device-Type", userRequest.getDeviceType());
                            }

                            if (userRequest.getDeviceId() != null) {
                                requestBuilder.header("X-Device-Id", userRequest.getDeviceId());
                            }

                            return requestBuilder.build();
                        }
                    }
                    return request;
                })
                // --- RATE LIMITING ---
                // Returns 429 Too Many Requests when the bucket is exhausted.
                // Redis timeout (3s, set in RateLimiterConfig) surfaces as a 500 only
                // if Redis is genuinely unreachable; normal throttling always returns 429.
                .filter(rateLimit(config -> config
                        .setCapacity(10)
                        .setPeriod(Duration.ofSeconds(1))
                        .setStatusCode(HttpStatus.TOO_MANY_REQUESTS) // 429 on rate limit exceeded
                        .setKeyResolver(request -> request.remoteAddress()
                                .map(addr -> "rate_limit:" + addr.getAddress().getHostAddress())
                                .orElse("rate_limit:anonymous"))
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