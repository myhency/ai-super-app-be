package io.hency.aisuperapp.features.topic.application.domain.entity;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.domain.entity.Identifiable;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record Topic(
        Ulid id,
        Ulid userId,
        String title,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        String updatedBy,
        String createdBy
) implements Identifiable {
    @Override
    public String getId() {
        return id.toString();
    }

    public static Topic of(TopicEntity topicEntity) {
        return Topic.builder()
                .id(topicEntity.getUlid())
                .userId(topicEntity.getUserId())
                .title(topicEntity.getTitle())
                .createdAt(topicEntity.getCreatedAt())
                .updatedAt(topicEntity.getUpdatedAt())
                .updatedBy(topicEntity.getUpdatedBy())
                .createdBy(topicEntity.getCreatedBy())
                .build();
    }
}
