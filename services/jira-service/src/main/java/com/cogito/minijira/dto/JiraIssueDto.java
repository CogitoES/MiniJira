package com.cogito.minijira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraIssueDto(
    String id,
    String key,
    Fields fields
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fields(
        String summary,
        Object description,
        Status status,
        Priority priority,
        User reporter
    ) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Status(String name) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Priority(String name) {}

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record User(String accountId) {}
    }
}
