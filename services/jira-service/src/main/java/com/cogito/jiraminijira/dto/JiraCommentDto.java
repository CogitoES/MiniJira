package com.cogito.jiraminijira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraCommentDto {
    private String id;
    private Object body;
    private Author author;

    @Data
    public static class Author { private String accountId; }
}
