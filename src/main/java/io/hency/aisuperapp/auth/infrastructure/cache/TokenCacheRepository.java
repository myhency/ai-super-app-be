package io.hency.aisuperapp.auth.infrastructure.cache;

import io.hency.aisuperapp.auth.application.domain.entity.Token;
import io.hency.aisuperapp.common.infrastructure.cache.BaseCacheRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TokenCacheRepository extends BaseCacheRepository<Token> {

    public TokenCacheRepository(ReactiveRedisTemplate<String, Token> redisTemplate) {
        super(redisTemplate);
    }
}
