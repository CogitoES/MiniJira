package com.cogito.jiraminijira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssueDto {
    private String id;
    private String key;
    private Fields fields;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {
        private String summary;
        private Object description;
        private Status status;
        private Priority priority;
        private User reporter;

        @Data
        public static class Status { private String name; }
        @Data
        public static class Priority { private String name; }
        @Data
        public static class User { private String accountId; }
    }
}
