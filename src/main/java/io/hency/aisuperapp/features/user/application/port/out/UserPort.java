package io.hency.aisuperapp.features.user.application.port.out;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.features.user.adapter.out.dto.TeamsUser;
import io.hency.aisuperapp.features.user.application.domain.entity.User;
import reactor.core.publisher.Mono;

public interface UserPort {
    Mono<User> findUserFromCacheBy(String accessToken);
    Mono<User> saveUserToCache(String accessToken, User user);
    Mono<TeamsUser> getTeamsUser(String accessToken);
    Mono<User> findUserByUserKey(String userKey);
    Mono<User> create(String tenantId, String userKey, String userName, String email, String subscribeId);
    Mono<User> updateLastAccessedAt(Ulid userId);
}
