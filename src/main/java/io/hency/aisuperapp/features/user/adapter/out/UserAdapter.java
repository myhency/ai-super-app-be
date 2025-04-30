package io.hency.aisuperapp.features.user.adapter.out;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.features.user.adapter.out.dto.TeamsUser;
import io.hency.aisuperapp.features.user.application.port.out.UserPort;
import io.hency.aisuperapp.features.user.application.domain.entity.User;
import io.hency.aisuperapp.features.user.application.domain.entity.UserEntity;
import io.hency.aisuperapp.infrastructure.client.MsGraphApiClient;
import io.hency.aisuperapp.infrastructure.repository.cache.UserCacheRepository;
import io.hency.aisuperapp.infrastructure.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserAdapter implements UserPort {
    private static final String KEY_PREFIX = "hmgOcean::user::";
    private final UserCacheRepository userCacheRepository;
    private final MsGraphApiClient msGraphApiClient;
    private final UserRepository userRepository;

    @Override
    public Mono<User> findUserFromCacheBy(String accessToken) {
        return userCacheRepository.findBy(KEY_PREFIX + accessToken);
    }

    @Override
    public Mono<User> saveUserToCache(String accessToken, User user) {
        return userCacheRepository.save(KEY_PREFIX + accessToken, user, Duration.ofHours(3));
    }

    @Override
    public Mono<TeamsUser> getTeamsUser(String accessToken) {
        return msGraphApiClient.getTeamsUser(accessToken)
                .doOnNext(getTeamsUserResponse -> {
                    log.debug("getTeamsUserResponse.getUserPrincipalName(): {}", getTeamsUserResponse.getUserPrincipalName());
                    log.debug("getTeamsUserResponse.getDisplayName(): {}", getTeamsUserResponse.getDisplayName());
                    log.debug("getTeamsUserResponse.getMail(): {}", getTeamsUserResponse.getMail());
                })
                .filter(getTeamsUserResponse ->
                        StringUtils.hasText(getTeamsUserResponse.getUserPrincipalName())
                )
                .map(getTeamsUserResponse ->
                        new TeamsUser(
                                getTeamsUserResponse.getId(),
                                getTeamsUserResponse.getMail(),
                                getTeamsUserResponse.getUserPrincipalName(),
                                getTeamsUserResponse.getDisplayName()
                        ));
    }

    @Override
    public Mono<User> findUserByUserKey(String userKey) {
        return userRepository.findByUserKey(userKey)
                .map(User::of);
    }

    @Override
    public Mono<User> create(String tenantId, String userKey, String userName, String email, String subscribeId) {
        return userRepository.save(UserEntity.of(tenantId, userKey, userName, email, subscribeId))
                .map(User::of)
                .onErrorResume(DuplicateKeyException.class, e ->
                        userRepository.findByUserKey(userKey).map(User::of)
                );
    }

    @Override
    public Mono<User> updateLastAccessedAt(Ulid userId) {
        return userRepository.findByUlid(userId)
                .flatMap(userEntity -> {
                    userEntity.updateLastAccessedAt(userEntity.getUlid());
                    return userRepository.save(userEntity);
                })
                .map(User::of);
    }
}
