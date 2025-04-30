package io.hency.aisuperapp.common.infrastructure.cache;

import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.InternalServerErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
public abstract class BaseCacheRepository<T> {
    private final ReactiveValueOperations<String, T> redisOps;

    protected BaseCacheRepository(ReactiveRedisTemplate<String, T> redisTemplate) {
        this.redisOps = redisTemplate.opsForValue();
    }

    public Mono<T> save(String key, T value, Duration ttl) {
        return redisOps.setIfAbsent(key, value, ttl)
                .then(Mono.just(value))
                .doOnError(error -> log.error("Something wrong with redis", error))
                .onErrorResume(e -> Mono.error(new InternalServerErrorException(ErrorCode.H500E)));
    }

    public Mono<T> findByKey(String key) {
        return redisOps.get(key)
                .doOnError(error -> log.error("Something wrong with redis", error))
                .onErrorResume(e -> Mono.error(new InternalServerErrorException(ErrorCode.H500E)));
    }
}
