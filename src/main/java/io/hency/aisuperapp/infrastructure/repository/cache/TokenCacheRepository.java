package io.hency.aisuperapp.infrastructure.repository.cache;

import io.hency.aisuperapp.auth.domain.entity.Token;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TokenCacheRepository extends BaseCacheRepository<Token> {

    public TokenCacheRepository(ReactiveRedisTemplate<String, Token> redisTemplate) {
        super(redisTemplate);
    }
}
