package io.hency.aisuperapp.features.chat.application.domain.entity;

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
@Table("chat_threads")
public class ChatThread {

    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("title")
    private String title;

    @Column("model_name")
    private String modelName;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("is_deleted")
    private Boolean isDeleted;
}
