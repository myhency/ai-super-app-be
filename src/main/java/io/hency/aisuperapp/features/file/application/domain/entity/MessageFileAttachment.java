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
@Table("message_file_attachments")
public class MessageFileAttachment {

    @Id
    private Long id;

    @Column("message_id")
    private Long messageId;

    @Column("file_attachment_id")
    private Long fileAttachmentId;

    @Column("created_at")
    private LocalDateTime createdAt;
}
