package io.hency.aisuperapp.features.topic.adapter.in.dto;

import io.hency.aisuperapp.features.topic.domain.entity.Topic;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record GenerateTopicResponse(
        String id,
        String title,
        ZonedDateTime createDateTime
) {
    public static GenerateTopicResponse of(Topic topic) {
        return GenerateTopicResponse.builder()
                .id(topic.id().toString())
                .title(topic.title())
                .createDateTime(topic.createdAt())
                .build();
    }
}
