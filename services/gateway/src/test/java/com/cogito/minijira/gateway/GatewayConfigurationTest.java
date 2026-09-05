package com.cogito.minijira.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class GatewayConfigurationTest {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    public void testRoutesDefinition() {
        Flux<Route> routes = routeLocator.getRoutes();

        StepVerifier.create(routes)
            .expectNextCount(5)
            .verifyComplete();
    }

    @Test
    public void testRouteMatchingEdgeCases() {
        // Test: /projects/1/tasks matches task-service
        verifyRouteMatch("/projects/1/tasks", "task-service");
        //verifyRouteMatch("/projects/1/tasks", "project-service");
        // Test: /projects/1 matches project-service
        verifyRouteMatch("/projects/1", "project-service");

        // Test: /projects matches project-service
        verifyRouteMatch("/projects", "project-service");

        // Test: /tasks/5 matches task-service
        verifyRouteMatch("/tasks/5", "task-service");

        // Test: /tasks/1/comments matches comment-service
        verifyRouteMatch("/tasks/1/comments", "comment-service");

        // Test: /auth/login matches auth-service
        verifyRouteMatch("/auth/login", "auth-service");

        // Test: /jira/issue matches jira-service
        verifyRouteMatch("/jira/issue", "jira-service");
    }

    /**
     * Verifies that a given path maps to the expected Gateway route.
     *
     * Logic:
     * 1. Creates a MockServerWebExchange representing an incoming HTTP GET request to the specified path.
     * 2. Iterates through all routes configured in the RouteLocator.
     * 3. For each route, it invokes the associated predicate (apply) with the mock exchange.
     *    - Since the predicate returns a reactive Mono<Boolean>, it is blocked synchronously
     *      to evaluate the result.
     * 4. Filters the route stream to find the first route whose predicate evaluates to true.
     * 5. Asserts that a route was found and that its ID matches the expectedRouteId.
     *
     * @param path the request path to simulate
     * @param expectedRouteId the expected ID of the route that should match this path
     */
    private void verifyRouteMatch(String path, String expectedRouteId) {
        ServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(path).build()
        );

        Route matchedRoute = routeLocator.getRoutes()
                .collectList()
                .block()
                .stream()
                .filter(route -> {
                    Boolean isMatch = Mono.from(route.getPredicate().apply(exchange)).block();
                    return Boolean.TRUE.equals(isMatch);
                })
                .findFirst()
                .orElse(null);

        assertNotNull(matchedRoute, "No route matched the given path: " + path);
        assertEquals(expectedRouteId, matchedRoute.getId());
    }
}
