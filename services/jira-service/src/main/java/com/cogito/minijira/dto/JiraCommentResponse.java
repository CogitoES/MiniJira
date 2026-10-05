package com.cogito.minijira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Jira Comment Response DTO - Represents comment data from Jira API
 * 
 * This record wraps the list of comments returned by Jira's comment endpoint,
 * facilitating the transfer of comment data from Jira to the internal system.
 * 
 * @param comments list of Jira comments
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraCommentResponse(
    List<JiraCommentDto> comments
) {}
