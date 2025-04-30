package io.hency.aisuperapp.features.topic.infrastructure.repository;

import io.hency.aisuperapp.features.topic.application.domain.entity.TopicEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@RequiredArgsConstructor
public class TopicRepositoryImpl implements TopicPaginationRepository {
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Flux<TopicEntity> selectByRowSize(Criteria criteria, int rowSize) {
        return r2dbcEntityTemplate.select(TopicEntity.class)
                .matching(Query.query(criteria)
                        .sort(Sort.by(Sort.Order.desc("id")))
                        .limit(rowSize)
                )
                .all();
    }
}
