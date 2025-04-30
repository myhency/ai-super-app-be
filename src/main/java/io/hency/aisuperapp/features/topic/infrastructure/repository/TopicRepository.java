package io.hency.aisuperapp.features.topic.infrastructure.repository;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.features.topic.application.domain.entity.TopicEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface TopicRepository extends R2dbcRepository<TopicEntity, Long>, TopicPaginationRepository {
    Mono<TopicEntity> findByUlidAndUserIdAndIsDeleted(Ulid ulid, Ulid userId, boolean deleted);
}
