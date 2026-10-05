package com.cogito.minijira.common.dto;

import lombok.Data;

/**
 * Task Data Transfer Object - Represents a task for API communication
 */
@Data
public class TaskDto {
    
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String jiraKey;
}
