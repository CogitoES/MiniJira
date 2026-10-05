package com.cogito.minijira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraSearchResponse(
    List<JiraIssueDto> issues,
    Integer startAt,
    Integer total,
    Integer maxResults
) {}
