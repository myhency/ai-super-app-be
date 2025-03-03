package io.hency.aisuperapp.features.chat.domain.entity;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.domain.entity.Identifiable;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record Chat(
        Ulid id,
        Ulid topicId,
        Ulid parentId,
        Message message,
        ZonedDateTime createdAt,
        String createdBy,
        ZonedDateTime updatedAt,
        String updatedBy
) implements Identifiable {
    @Override
    public String getId() {
        return id.toString();
    }

    public static Chat of(ChatEntity chatEntity, String messageContent) {
        return Chat.builder()
                .id(chatEntity.getUlid())
                .topicId(chatEntity.getTopicId())
                .parentId(chatEntity.getParentId())
                .message(new Message(messageContent, chatEntity.getRole()))
                .createdAt(chatEntity.getCreatedAt())
                .createdBy(chatEntity.getCreatedBy())
                .updatedAt(chatEntity.getUpdatedAt())
                .updatedBy(chatEntity.getUpdatedBy())
                .build();
    }

    public static Chat of(ChatEntity chatEntity) {
        return Chat.builder()
                .id(chatEntity.getUlid())
                .topicId(chatEntity.getTopicId())
                .parentId(chatEntity.getParentId())
                .message(new Message(chatEntity.getContent(), chatEntity.getRole()))
                .createdAt(chatEntity.getCreatedAt())
                .createdBy(chatEntity.getCreatedBy())
                .updatedAt(chatEntity.getUpdatedAt())
                .updatedBy(chatEntity.getUpdatedBy())
                .build();
    }
}
