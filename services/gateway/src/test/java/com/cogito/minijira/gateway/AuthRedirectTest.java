package com.cogito.minijira.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthRedirectTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testRegisterEndpointDoesNotRedirect() {
        String json = "{\"email\":\"test@example.com\", \"password\":\"password\", \"username\":\"testuser\"}";
        webTestClient.post()
                .uri("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .exchange()
                .expectStatus().isOk();
    }
}

