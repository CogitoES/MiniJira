package com.cogito.minijira;

import com.cogito.minijira.domain.Task;
import com.cogito.minijira.repository.TaskRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    public void setUp() {
        taskRepository.deleteAll();
    }
    @Value("${app.jwt.secret}")
    private String secret;

    private String generateToken(String username) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes());
        return Jwts.builder()
                .subject(username)
                .claim("userId", 1L)
                .signWith(key)
                .compact();
    }


    @Test
    @WithMockUser
    public void testDeleteTask() throws Exception {
        String token = generateToken("testuser");
        Task task = new Task();
        task.setTitle("Test Task");
        task.setProjectId(1L);
        task.setStatus("TODO");
        task.setPriority("MEDIUM");
        task.setReporterId(1L);
        task = taskRepository.save(task);

        mockMvc.perform(delete("/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/projects/1/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void testGetTasksByProjectId() throws Exception {
        String token = generateToken("testuser");
        Task task = new Task();
        task.setProjectId(1L);
        task.setTitle("Project Task");
        task.setStatus("TODO");
        task.setPriority("MEDIUM");
        task.setReporterId(1L);
        taskRepository.save(task);

        mockMvc.perform(get("/projects/1/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Project Task"));
    }

    @Test
    @WithMockUser
    public void testCreateTask() throws Exception {
        String token = generateToken("testuser");
        String taskJson = "{\"title\":\"New Task\", \"status\":\"TODO\", \"priority\":\"MEDIUM\", \"reporterId\": 1}";

        mockMvc.perform(post("/projects/1/tasks")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(taskJson))
                .andExpect(status().isOk());
    }
}
