package com.cogito.minijira.common.dto;

/**
 * Project Request DTO - Represents project creation and update data
 * 
 * This DTO contains the required fields for creating or updating a project,
 * including name, description, status, and external Jira references.
 */
public class ProjectRequest {
    
    private String name;
    private String description;
    private String status;
    private String jiraKey;

    public ProjectRequest() {
    }

    // ========== Getters and Setters ==========

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public String getJiraKey() {
        return jiraKey;
    }
    
    public void setJiraKey(String jiraKey) {
        this.jiraKey = jiraKey;
    }
}
