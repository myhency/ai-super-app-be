package io.hency.aisuperapp.features.file.adapter.in.dto;

import io.hency.aisuperapp.features.file.application.domain.entity.FileAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    private Long fileId;
    private String fileName;
    private String originalName;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime createdAt;

    public static FileUploadResponse from(FileAttachment fileAttachment) {
        return FileUploadResponse.builder()
            .fileId(fileAttachment.getId())
            .fileName(fileAttachment.getFileName())
            .originalName(fileAttachment.getOriginalName())
            .fileSize(fileAttachment.getFileSize())
            .mimeType(fileAttachment.getMimeType())
            .createdAt(fileAttachment.getCreatedAt())
            .build();
    }
}
