package io.hency.aisuperapp.features.file.application.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("file_attachments")
public class FileAttachment {

    @Id
    private Long id;

    @Column("file_name")
    private String fileName;

    @Column("original_name")
    private String originalName;

    @Column("file_path")
    private String filePath;

    @Column("file_size")
    private Long fileSize;

    @Column("mime_type")
    private String mimeType;

    @Column("uploaded_by")
    private Long uploadedBy;

    @Column("created_at")
    private LocalDateTime createdAt;
}
