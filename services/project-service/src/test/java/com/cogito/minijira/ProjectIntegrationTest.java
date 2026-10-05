package com.cogito.minijira;

import com.cogito.minijira.client.AuthClient;
import com.cogito.minijira.domain.Project;
import com.cogito.minijira.common.dto.ProjectRequest;
import com.cogito.minijira.repository.ProjectRepository;
import com.cogito.minijira.service.ProjectService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class ProjectIntegrationTest {


    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    @MockitoBean
    private AuthClient authClient;

    @BeforeEach
    public void setUp() {
        projectRepository.deleteAll();
        Mockito.when(authClient.exists(Mockito.anyLong())).thenReturn(true);
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
    public void testCreateAndGetProject() {
        ProjectRequest request = new ProjectRequest();
        request.setName("Test Project");
        request.setDescription("Description");
        request.setStatus("ACTIVE");

        Project created = projectService.createProject(request, 1L);
        assertThat(created.getId()).isNotNull();
        assertThat(projectService.getAllProjects()).hasSize(1);
    }

    @Test
    @WithMockUser
    public void testGetProjectById() throws Exception {
        String token = generateToken("testuser");
        Project project = new Project();
        project.setName("Test Project");
        project.setOwnerId(1L);
        project.setStatus("ACTIVE");
        project = projectRepository.save(project);

        mockMvc.perform(get("/projects/" + project.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    @WithMockUser
    public void testGetAllProjects() throws Exception {
        String token = generateToken("testuser");
        Project project = new Project();
        project.setName("Test Project");
        project.setOwnerId(1L);
        project.setStatus("ACTIVE");
        projectRepository.save(project);

        mockMvc.perform(get("/projects")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Project"));
    }

    @Test
    @WithMockUser
    public void testCreateProject() throws Exception {
        String token = generateToken("testuser");
        String projectJson = "{\"name\":\"New Project\", \"status\":\"ACTIVE\"}";

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(projectJson))
                .andExpect(status().isOk());
    }

    @Test
    void createProject_UpdatesExisting() throws Exception {
         ProjectRequest request = new ProjectRequest();
         request.setName("Test Project");
         request.setDescription("Initial Description");
         request.setStatus("ACTIVE");
         String token = generateToken("testuser");

         String json = "{\"name\":\"Test Project\", \"description\":\"Initial Description\", \"status\":\"ACTIVE\"}";

         // First request creates
         mockMvc.perform(post("/projects")
                 .contentType(MediaType.APPLICATION_JSON)
                 .header("Authorization", "Bearer " + token)
                 .content(json))
                 .andExpect(status().isOk());

         // Second request updates
         String updateJson = "{\"name\":\"Test Project\", \"description\":\"Updated Description\", \"status\":\"ACTIVE\"}";
         mockMvc.perform(post("/projects")
                         .contentType(MediaType.APPLICATION_JSON)
                         .header("Authorization", "Bearer " + token)
                         .content(updateJson))
                 .andExpect(status().isOk());
         
         assertThat(projectRepository.findByName("Test Project").get().getDescription()).isEqualTo("Updated Description");
    }
}
