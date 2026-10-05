package com.cogito.minijira;

import com.cogito.minijira.client.AuthClient;
import com.cogito.minijira.domain.Comment;
import com.cogito.minijira.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @MockitoBean
    private AuthClient authClient;

    @BeforeEach
    public void setUp() {
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
    @WithMockUser(username = "testuser")
    public void testGetCommentsByTaskId() throws Exception {
        String token = generateToken("testuser");
        Comment comment = new Comment();
        comment.setTaskId(1L);
        comment.setText("Test comment");
        comment.setUserId(1L); // Explicitly set user ID for this test
        commentRepository.save(comment);

        mockMvc.perform(get("/tasks/1/comments")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Test comment"));
    }

    @Test
    @WithMockUser(username = "testuser")
    public void testCreateComment() throws Exception {
        String token = generateToken("testuser");
        String commentJson = "{\"text\":\"New comment\"}";

        mockMvc.perform(post("/tasks/1/comments")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("New comment"));
    }
}
