package io.hency.aisuperapp.common.infrastructure.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hency.aisuperapp.auth.application.domain.entity.Token;
import io.hency.aisuperapp.features.user.application.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {
    private static final Duration DEFAULT_TTL = Duration.ofMillis(1000);

    @Value("${spring.data.redis.port}")
    private int port;

    @Value("${spring.data.redis.host}")
    private String host;

    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return standaloneConnectionFactory();
    }

    private ReactiveRedisConnectionFactory standaloneConnectionFactory() {
        var configuration = LettuceClientConfiguration.builder()
                .commandTimeout(DEFAULT_TTL)
                .build();
        return new LettuceConnectionFactory(
                new RedisStandaloneConfiguration(
                        host,
                        port
                ),
                configuration
        );
    }

    @Bean(name = "tokenRedisTemplate")
    @DependsOn("reactiveRedisConnectionFactory")
    public ReactiveRedisTemplate<String, Token> tokenRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory, ObjectMapper objectMapper) {
        var keySerializer = new StringRedisSerializer();
        var valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Token.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, Token> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);
        var context = builder.value(valueSerializer).build();
        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, context);
    }

    @Bean(name = "userRedisTemplate")
    @DependsOn("reactiveRedisConnectionFactory")
    public ReactiveRedisTemplate<String, User> userRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory, ObjectMapper objectMapper) {
        var keySerializer = new StringRedisSerializer();
        var valueSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, User.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, User> builder =
                RedisSerializationContext.newSerializationContext(keySerializer);
        var context = builder.value(valueSerializer).build();
        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, context);
    }

    @Bean(name = "reactiveStringRedisTemplate")
    @DependsOn("reactiveRedisConnectionFactory")
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
        return new ReactiveStringRedisTemplate(reactiveRedisConnectionFactory);
    }
}
