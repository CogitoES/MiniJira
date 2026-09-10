package com.cogito.jiraminijira.dto;

import lombok.Data;
import java.util.List;

@Data
public class JiraCommentResponse {
    private List<JiraCommentDto> comments;
}
