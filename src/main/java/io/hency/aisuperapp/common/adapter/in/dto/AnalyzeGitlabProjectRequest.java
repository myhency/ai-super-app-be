package io.hency.aisuperapp.common.adapter.in.dto;

import lombok.Data;

@Data
public class AnalyzeGitlabProjectRequest {
    private String token;
    private String repoUrl;
}
