package com.cogito.minijira.common.dto;

import lombok.Data;

/**
 * Comment Data Transfer Object - Represents a comment for API communication
 */
@Data
public class CommentDto {
    
    private Long id;
    private String text;
    private String jiraKey;
    private Long taskId;
    private Long userId;
}
