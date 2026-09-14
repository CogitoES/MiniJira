package com.cogito.minijira.common.dto;

import lombok.Data;

@Data
public class CommentDto {
    private Long id;
    private String text;
    private String jiraKey;
    private Long taskId;
    private Long userId;
}
