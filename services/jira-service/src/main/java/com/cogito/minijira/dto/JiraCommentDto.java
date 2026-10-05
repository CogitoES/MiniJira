package com.cogito.minijira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jira Comment DTO - Represents a comment from Jira API
 * 
 * This record captures a single comment retrieved from Jira, including
 * the comment text and author information, used for comment synchronization.
 * 
 * @param id unique identifier of the comment
 * @param body text content of the comment
 * @param author author/creator of the comment
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraCommentDto(
    String id,
    Object body,
    Author author
) {
    /**
     * Comment Author - Represents the user who created a comment
     * 
     * @param accountId unique account ID of the author
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Author(String accountId) {}
}
