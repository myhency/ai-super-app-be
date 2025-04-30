package io.hency.aisuperapp.features.topic.adapter.out;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.features.topic.application.port.out.TopicPort;
import io.hency.aisuperapp.features.topic.application.domain.entity.TopicEntity;
import io.hency.aisuperapp.features.topic.infrastructure.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class TopicAdapter implements TopicPort {
    private final TopicRepository topicRepository;

    @Override
    public Mono<TopicEntity> findTopicByTopicIdAndUserId(Ulid topicId, Ulid userId) {
        return topicRepository.findByUlidAndUserIdAndIsDeleted(topicId, userId, false);
    }

    @Override
    public Mono<TopicEntity> save(TopicEntity topicEntity) {
        return topicRepository.save(topicEntity);
    }
}
