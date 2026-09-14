package com.cogito.minijira.config;

import com.cogito.minijira.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("dev")
public class DatabaseCleanerConfig implements CommandLineRunner {

    private final TaskRepository repository;

    public DatabaseCleanerConfig(TaskRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        repository.deleteAll();
    }
}
