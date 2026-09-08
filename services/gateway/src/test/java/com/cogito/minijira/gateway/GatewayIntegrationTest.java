package com.cogito.minijira.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@EnableWireMock(
        @ConfigureWireMock(baseUrlProperties = "services.project.uri")
)
class GatewayIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testProjectRoute() {
        // WireMock:
        // GET /projects/123 -> 200
        stubFor(get(urlEqualTo("/projects/123"))
                .willReturn(aResponse().withStatus(200)));

        webTestClient
                .get()
                .uri("/projects/123")
                .exchange()
                .expectStatus()
                .isOk();
    }
}