package com.cogito.minijira.gateway;

import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.gateway.route.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GatewayConfiguration.class);

    @Bean
    public GlobalFilter loggingFilter() {
        return (exchange, chain) -> {
            Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
            String routeId = (route != null) ? route.getId() : "pending-route";
            
            logger.info(">>> REQUEST: {} {} - Route: {} - Origin: {} - Headers: {}", 
                    exchange.getRequest().getMethod(), 
                    exchange.getRequest().getURI(), 
                    routeId,
                    exchange.getRequest().getHeaders().getOrigin(),
                    exchange.getRequest().getHeaders());
            
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                logger.info("<<< RESPONSE: {} {} - Status: {} - Headers: {}", 
                        exchange.getRequest().getMethod(), 
                        exchange.getRequest().getURI(), 
                        exchange.getResponse().getStatusCode(),
                        exchange.getResponse().getHeaders());
            }));
        };
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r
                        .path("/auth/**")
                        .uri("http://localhost:8082"))
                .route("project-service", r -> r
                        .path("/projects/**")
                        .and().not(p -> p.path("/projects/*/tasks/**"))
                        .uri("http://localhost:8085"))
                .route("task-service", r -> r
                        .path("/projects/*/tasks/**", "/tasks/**")
                        .and().not(p -> p.path("/tasks/*/comments"))
                        .uri("http://localhost:8086"))
                .route("comment-service", r -> r
                        .path("/tasks/*/comments")
                        .uri("http://localhost:8083"))
                .route("jira-service", r -> r
                        .path("/jira/**")
                        .uri("http://localhost:8084"))
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.addAllowedOrigin("http://localhost:5173");
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}

