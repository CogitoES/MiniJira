package com.cogito.minijira.common.dto;

import lombok.Data;

/**
 * Project Data Transfer Object - Represents a project for API communication
 */
@Data
public class ProjectDto {
    
    private Long id;
    private String name;
    private String description;
    private String status;
    private String jiraKey;
}
