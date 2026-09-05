package com.cogito.minijira;

import com.cogito.minijira.domain.Task;
import com.cogito.minijira.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdditionalTaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    @WithMockUser
    public void testGetTasksByProjectId() throws Exception {
        Task task = new Task();
        task.setProjectId(1L);
        task.setTitle("Project Task");
        taskRepository.save(task);

        mockMvc.perform(get("/projects/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Project Task"));
    }

    @Test
    @WithMockUser
    public void testCreateTask() throws Exception {
        String taskJson = "{\"title\":\"New Task\"}";

        mockMvc.perform(post("/projects/1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson))
                .andExpect(status().isOk());
    }
}
