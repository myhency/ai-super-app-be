package io.hency.aisuperapp.features.onedrive.application.domain.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OneDriveFile {
    private String id;
    private String name;
    private String mimeType;
    private Long size;
    private String downloadUrl;
    private String webUrl;
    private LocalDateTime createdDateTime;
    private LocalDateTime lastModifiedDateTime;
    private boolean isFolder;
    private String parentId;
}