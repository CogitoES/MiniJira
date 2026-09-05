package com.cogito.authminijira;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testRegistrationAndLogin() throws Exception {
        String registerJson = "{\"email\":\"test@example.com\", \"password\":\"password\", \"username\":\"testuser\"}";
        
        // 1. Test Registration
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // 2. Test Login
        String loginJson = "{\"email\":\"test@example.com\", \"password\":\"password\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").exists());
    }

    @Test
    public void testInvalidLogin() throws Exception {
        String loginJson = "{\"email\":\"wrong@example.com\", \"password\":\"wrongpassword\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    public void testDuplicateRegistration() throws Exception {
        String registerJson = "{\"email\":\"dup@example.com\", \"password\":\"password\", \"username\":\"dupuser\"}";
        
        // First registration
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(MockMvcResultMatchers.status().isOk());

        // Second registration (should fail)
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}
