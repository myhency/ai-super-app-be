package io.hency.aisuperapp.common.infrastructure.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class StringCacheRepository extends BaseCacheRepository<String> {

    public StringCacheRepository(ReactiveStringRedisTemplate reactiveStringRedisTemplate) {
        super(reactiveStringRedisTemplate);
    }
}
