package io.hency.aisuperapp.features.userlimit.infrastructure.repository;

import io.hency.aisuperapp.features.userlimit.application.domain.entity.UserLimitEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserLimitRepository extends ReactiveCrudRepository<UserLimitEntity, Long> {
    Mono<UserLimitEntity> findByUserKey(String userKey);
}
