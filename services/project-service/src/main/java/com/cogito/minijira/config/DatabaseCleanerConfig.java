package com.cogito.minijira.config;

import com.cogito.minijira.repository.ProjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DatabaseCleanerConfig implements CommandLineRunner {

    private final ProjectRepository repository;

    public DatabaseCleanerConfig(ProjectRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        repository.deleteAll();
    }
}
