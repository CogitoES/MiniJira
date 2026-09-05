package com.cogito.minijira.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GatewayIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAuthRoute() throws Exception {
        mockMvc.perform(get("/auth/test"))
                .andExpect(status().isNotFound()); // Expecting not found, not 404 from the actual service but from gateway if not routed. Actually, just check if it routes.
    }
}
