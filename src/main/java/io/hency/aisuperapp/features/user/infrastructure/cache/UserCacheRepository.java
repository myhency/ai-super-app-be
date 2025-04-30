package io.hency.aisuperapp.features.user.infrastructure.cache;

import io.hency.aisuperapp.features.user.application.domain.entity.User;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
public class UserCacheRepository {
    private final ReactiveValueOperations<String, User> redisOps;

    public UserCacheRepository(ReactiveRedisTemplate<String, User> redisTemplate) {
        this.redisOps = redisTemplate.opsForValue();
    }

    public Mono<User> save(String key, User value, Duration ttl) {
        return redisOps.setIfAbsent(key, value, ttl)
                .then(Mono.just(value));
    }

    public Mono<User> findBy(String key) {
        return redisOps.get(key);
    }
}
