package com.cogito.minijira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraCommentDto(
    String id,
    Object body,
    Author author
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Author(String accountId) {}
}
