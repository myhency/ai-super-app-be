package io.hency.aisuperapp.common.adapter.in.dto;

import lombok.Data;

@Data
public class CloneRequest {
    private String token;
    private String repoUrl;
    private String targetDir;
}
