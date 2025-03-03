package io.hency.aisuperapp.infrastructure.repository.chat;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.features.chat.domain.entity.ChatEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface ChatRepository extends R2dbcRepository<ChatEntity, Long>, ChatPaginationRepository {
    Mono<ChatEntity> findByUlid(Ulid ulid);

    Mono<ChatEntity> findTopByTopicIdOrderById(Ulid topicId);
}
