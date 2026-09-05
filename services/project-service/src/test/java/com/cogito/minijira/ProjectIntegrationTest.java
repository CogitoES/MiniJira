package com.cogito.minijira;

import com.cogito.minijira.domain.Project;
import com.cogito.minijira.common.dto.ProjectRequest;
import com.cogito.minijira.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class ProjectIntegrationTest {

    @Autowired
    private ProjectService projectService;

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
}
