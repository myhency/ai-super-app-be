package io.hency.aisuperapp.infrastructure.config.web.context;

import io.hency.aisuperapp.features.user.domain.entity.User;
import reactor.core.publisher.Mono;

public class UserContextHolder {
    public static final String USER_KEY = "user";

    public static Mono<User> getUserMono() {
        return Mono.deferContextual(contextView ->
            Mono.just(contextView.get(USER_KEY))
        );
    }
}
