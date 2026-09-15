package com.cogito.minijira.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.cloud.gateway.route.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GatewayConfiguration.class);

    @Value("${services.auth.uri:http://localhost:8082}")
    private String authServiceUri;
    @Value("${services.project.uri:http://localhost:8085}")
    private String projectServiceUri;
    @Value("${services.task.uri:http://localhost:8086}")
    private String taskServiceUri;
    @Value("${services.comment.uri:http://localhost:8083}")
    private String commentServiceUri;
    @Value("${services.jira.uri:http://localhost:8084}")
    private String jiraServiceUri;

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
                        .uri(authServiceUri))
                .route("project-service", r -> r
                        .path("/projects/**")
                        .and().not(p -> p.path("/projects/*/tasks/**"))
                        .uri(projectServiceUri))
                .route("task-service", r -> r
                        .path("/projects/*/tasks/**", "/tasks/**")
                        .and().not(p -> p.path("/tasks/*/comments", "/tasks/comments/**"))
                        .uri(taskServiceUri))
                .route("comment-service", r -> r
                        .path("/tasks/*/comments", "/tasks/comments/**", "/comments/**")
                        .uri(commentServiceUri))
                .route("jira-service", r -> r
                        .path("/jira/**")
                        .uri(jiraServiceUri))
                .build();
    }

    // Wrap the RouteLocator in a custom implementation or add a filter to log matching?
    // Since I cannot easily change the RouteLocator structure, I will add a GlobalFilter to log the route ID.
    // The current loggingFilter already logs the route ID.
    // Let me check why it's still "task-service".
    // Ah, the route "task-service" matches "/tasks/**" and it doesn't have an `and().not(...)` condition that excludes "/tasks/comments/**".
    // Let me update the task-service route to also exclude "/tasks/comments/**".

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

