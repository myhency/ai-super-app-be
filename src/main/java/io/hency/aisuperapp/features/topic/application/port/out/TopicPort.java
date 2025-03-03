package io.hency.aisuperapp.features.topic.application.port.out;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.features.topic.domain.entity.TopicEntity;
import reactor.core.publisher.Mono;

public interface TopicPort {
    Mono<TopicEntity> findTopicByTopicIdAndUserId(Ulid topicId, Ulid userId);
    Mono<TopicEntity> save(TopicEntity topicEntity);
}
