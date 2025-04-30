package io.hency.aisuperapp.features.topic.infrastructure.repository;

import io.hency.aisuperapp.features.topic.application.domain.entity.TopicEntity;
import org.springframework.data.relational.core.query.Criteria;
import reactor.core.publisher.Flux;

public interface TopicPaginationRepository {
    Flux<TopicEntity> selectByRowSize(Criteria criteria, int rowSize);
}
