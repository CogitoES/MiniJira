package com.cogito.minijira.repository;

import com.cogito.minijira.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectId(Long projectId);
    Optional<Task> findByJiraKey(String jiraKey);
    Optional<Task> findByTitle(String title);
}
