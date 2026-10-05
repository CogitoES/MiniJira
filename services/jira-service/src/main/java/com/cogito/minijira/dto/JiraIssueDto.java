package com.cogito.minijira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jira Issue DTO - Represents a Jira issue from the Jira API
 * 
 * This record captures the issue information retrieved from Jira including
 * identification, fields (summary, description, status, priority, reporter),
 * and is used for issue synchronization.
 * 
 * @param id unique identifier of the Jira issue
 * @param key Jira issue key (e.g., PROJ-123)
 * @param fields issue details containing summary, description, and metadata
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraIssueDto(
    String id,
    String key,
    Fields fields
) {
    /**
     * Issue Fields - Contains detailed information about a Jira issue
     * 
     * @param summary title/summary of the issue
     * @param description detailed description of the issue
     * @param status current status of the issue
     * @param priority priority level of the issue
     * @param reporter user who reported the issue
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fields(
        String summary,
        Object description,
        Status status,
        Priority priority,
        User reporter
    ) {
        /**
         * Status - Represents the status of a Jira issue
         * 
         * @param name status name (e.g., Open, In Progress, Done)
         */
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Status(String name) {}

        /**
         * Priority - Represents the priority of a Jira issue
         * 
         * @param name priority name (e.g., Low, Medium, High)
         */
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Priority(String name) {}

        /**
         * User - Represents a Jira user
         * 
         * @param accountId unique account ID of the user
         */
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record User(String accountId) {}
    }
}
