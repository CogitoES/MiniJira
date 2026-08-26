package com.cogito.minijira.repository;

import com.cogito.minijira.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByJiraKey(String jiraKey);
    boolean existsByName(String name);
}
