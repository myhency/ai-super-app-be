package io.hency.aisuperapp.auth.application.port.in;

import io.hency.aisuperapp.auth.domain.entity.Token;
import io.hency.aisuperapp.features.user.domain.entity.User;
import reactor.core.publisher.Mono;

public interface TeamsAuthUseCase {
    Mono<User> authorize(String accessToken, String ipAddress);
    Mono<Token> refreshToken(String expiredToken);
}
