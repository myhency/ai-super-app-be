package io.hency.aisuperapp.infrastructure.repository.user;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.features.user.domain.entity.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserRepository extends ReactiveCrudRepository<UserEntity, Long> {
    Mono<UserEntity> findByUserKey(String userKey);

    Mono<UserEntity> findByUlid(Ulid ulid);
}
