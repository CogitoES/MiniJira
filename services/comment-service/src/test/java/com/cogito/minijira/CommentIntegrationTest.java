package com.cogito.minijira;

import com.cogito.minijira.domain.Comment;
import com.cogito.minijira.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @WithMockUser
    public void testGetCommentsByTaskId() throws Exception {
        Comment comment = new Comment();
        comment.setTaskId(1L);
        comment.setText("Test comment");
        commentRepository.save(comment);

        mockMvc.perform(get("/tasks/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Test comment"));
    }

    @Test
    @WithMockUser
    public void testCreateComment() throws Exception {
        String commentJson = "{\"text\":\"New comment\"}";

        mockMvc.perform(post("/tasks/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(commentJson))
                .andExpect(status().isOk());
    }
}
