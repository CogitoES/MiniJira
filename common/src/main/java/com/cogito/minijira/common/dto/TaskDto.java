package com.cogito.minijira.common.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TaskDto {
    private String title;
    private String description;
    private String status;
    private String priority;
    private String jiraKey;
}
