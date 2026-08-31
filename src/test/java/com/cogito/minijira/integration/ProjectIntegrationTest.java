package com.cogito.minijira.integration;

import com.cogito.minijira.domain.Project;
import com.cogito.minijira.domain.User;
import com.cogito.minijira.common.dto.ProjectRequest;
import com.cogito.minijira.repository.ProjectRepository;
import com.cogito.minijira.repository.UserRepository;
import com.cogito.minijira.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ProjectIntegrationTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setEmail("owner@test.com");
        owner.setUsername("owner");
        owner.setEncryptedPassword("password");
        owner.setRole("USER");
        owner = userRepository.save(owner);
    }

    @Test
    void createAndRetrieveProject() {
        ProjectRequest request = new ProjectRequest();
        request.setName("Integration Project");
        request.setDescription("Integration Description");
        request.setStatus("OPEN");

        Project created = projectService.createProject(request, owner.getId());
        assertNotNull(created.getId());

        Project retrieved = projectRepository.findById(created.getId()).orElseThrow();
        assertEquals("Integration Project", retrieved.getName());
    }
}
