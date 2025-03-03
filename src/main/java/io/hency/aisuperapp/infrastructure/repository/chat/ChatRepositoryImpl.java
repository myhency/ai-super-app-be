package io.hency.aisuperapp.infrastructure.repository.chat;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.domain.enums.SliceDirection;
import io.hency.aisuperapp.features.chat.domain.entity.ChatEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ChatRepositoryImpl implements ChatPaginationRepository {
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Flux<ChatEntity> findPagedChats(Ulid topicId, Ulid baseId, SliceDirection sliceDirection, int size) {
        Criteria criteria = Criteria
                .where("topic_id").is(topicId)
                .and("is_deleted").is(false);

        criteria = this.addBaseIdCondition(baseId, sliceDirection, criteria);

        return r2dbcEntityTemplate.select(ChatEntity.class)
                .matching(Query.query(criteria)
                        .sort(Sort.by(Sort.Order.desc("id")))
                        .limit(size))
                .all();
    }

    private Criteria addBaseIdCondition(Ulid baseId, SliceDirection sliceDirection, Criteria criteria) {
        if (baseId == null) {
            return criteria;
        }

        return switch (sliceDirection) {
            case PREVIOUS -> criteria.and("ulid").greaterThan(baseId.toString());
            case NEXT -> criteria.and("ulid").lessThan(baseId.toString());
        };
    }
}
