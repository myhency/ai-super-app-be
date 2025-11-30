package io.hency.aisuperapp.features.chat.adapter.in.dto;

import io.hency.aisuperapp.features.chat.application.domain.entity.ChatThread;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatThreadResponse {

    private Long id;
    private Long userId;
    private String title;
    private String modelName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ChatThreadResponse from(ChatThread thread) {
        return ChatThreadResponse.builder()
                .id(thread.getId())
                .userId(thread.getUserId())
                .title(thread.getTitle())
                .modelName(thread.getModelName())
                .createdAt(thread.getCreatedAt())
                .updatedAt(thread.getUpdatedAt())
                .build();
    }
}
