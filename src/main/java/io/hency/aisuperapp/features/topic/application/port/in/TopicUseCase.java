package io.hency.aisuperapp.features.topic.application.port.in;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.features.topic.application.domain.entity.Topic;
import reactor.core.publisher.Mono;

public interface TopicUseCase {
    Mono<Topic> generate(Ulid ulid, Ulid userId);
    Mono<Topic> createDefaultTopic(Ulid topicId, Ulid userId);
}
