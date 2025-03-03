package io.hency.aisuperapp.features.user.application.port.in;

import io.hency.aisuperapp.features.user.adapter.out.dto.TeamsUser;
import io.hency.aisuperapp.features.user.domain.entity.User;
import reactor.core.publisher.Mono;

public interface UserUseCase {
    Mono<User> createUser(String accessToken, TeamsUser teamsUser);
}
