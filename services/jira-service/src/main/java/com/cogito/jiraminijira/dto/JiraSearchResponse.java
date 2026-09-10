package com.cogito.jiraminijira.dto;

import lombok.Data;
import java.util.List;

@Data
public class JiraSearchResponse {

    private List<JiraIssueDto> issues;
    private int startAt;
    private int total;
    private int maxResults;

    public List<JiraIssueDto> getIssues() {
        return issues;
    }

    public void setIssues(List<JiraIssueDto> issues) {
        this.issues = issues;
    }

    public int getStartAt() {
        return startAt;
    }

    public void setStartAt(int startAt) {
        this.startAt = startAt;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
}
