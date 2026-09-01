package com.cogito.minijira.gateway;

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
            logger.info(">>> REQUEST: {} {} - Origin: {} - Headers: {}", 
                    exchange.getRequest().getMethod(), 
                    exchange.getRequest().getURI(), 
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
                        .uri("http://127.0.0.1:8082"))
                .route("main-service", r -> r
                        .path("/projects/**", "/tasks/**")
                        .uri("http://localhost:8083"))
                .build();
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowCredentials(true);
        corsConfig.addAllowedOrigin("http://localhost:3000");
        corsConfig.addAllowedOrigin("http://localhost:5173");
        corsConfig.addAllowedHeader("*");
        corsConfig.addAllowedMethod("*");
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}

