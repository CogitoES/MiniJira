package com.cogito.minijira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * Jira Search Response DTO - Represents search results from Jira API
 * 
 * This record captures the paginated search results from Jira's issue search endpoint,
 * including the list of issues found and pagination metadata.
 * 
 * @param issues list of Jira issues matching the search criteria
 * @param startAt starting index of the current result set
 * @param total total number of issues matching the search criteria
 * @param maxResults maximum number of results returned per page
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraSearchResponse(
    List<JiraIssueDto> issues,
    Integer startAt,
    Integer total,
    Integer maxResults
) {}
