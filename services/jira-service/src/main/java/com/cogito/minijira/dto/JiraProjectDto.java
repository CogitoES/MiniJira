package com.cogito.minijira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jira Project DTO - Represents a Jira project from the Jira API
 * 
 * This record captures essential project information retrieved from Jira,
 * used for project synchronization and mapping.
 * 
 * @param id unique identifier of the Jira project
 * @param key Jira project key (e.g., PROJ)
 * @param name display name of the project
 * @param description detailed description of the project
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraProjectDto(
    String id,
    String key,
    String name,
    String description
) {}
