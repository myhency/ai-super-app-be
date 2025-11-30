package io.hency.aisuperapp.features.chat.infrastructure.repository;

import io.hency.aisuperapp.features.chat.application.domain.entity.Message;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageRepository extends ReactiveCrudRepository<Message, Long> {

    Flux<Message> findByThreadIdOrderByCreatedAtAsc(Long threadId);
}
