package io.hency.aisuperapp.features.chat.infrastructure.repository;

import io.hency.aisuperapp.features.chat.application.domain.entity.ChatThread;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ChatThreadRepository extends ReactiveCrudRepository<ChatThread, Long> {

    Flux<ChatThread> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(Long userId);

    Mono<ChatThread> findByIdAndIsDeletedFalse(Long id);
}
