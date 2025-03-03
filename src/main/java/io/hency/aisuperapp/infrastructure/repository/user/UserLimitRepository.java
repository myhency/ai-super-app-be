package io.hency.aisuperapp.infrastructure.repository.user;

import io.hency.aisuperapp.features.userlimit.domain.entity.UserLimitEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserLimitRepository extends ReactiveCrudRepository<UserLimitEntity, Long> {
    Mono<UserLimitEntity> findByUserKey(String userKey);
}
