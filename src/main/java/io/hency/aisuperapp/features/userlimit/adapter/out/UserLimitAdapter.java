package io.hency.aisuperapp.features.userlimit.adapter.out;

import io.hency.aisuperapp.auth.application.domain.vo.AccessType;
import io.hency.aisuperapp.features.user.application.domain.entity.User;
import io.hency.aisuperapp.features.userlimit.application.port.out.UserLimitPort;
import io.hency.aisuperapp.features.userlimit.application.domain.entity.UserLimitEntity;
import io.hency.aisuperapp.features.userlimit.infrastructure.repository.UserLimitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserLimitAdapter implements UserLimitPort {
    private final UserLimitRepository userLimitRepository;

    @Override
    public Mono<UserLimitEntity> upsertUserAccessType(User user, AccessType accessType) {
        return userLimitRepository.findByUserKey(user.userKey())
                .flatMap(userLimitEntity -> {
                    userLimitEntity.setAccessType(accessType);
                    return userLimitRepository.save(userLimitEntity);
                })
                .switchIfEmpty(createNewUserLimit(user, accessType));
    }

    private Mono<UserLimitEntity> createNewUserLimit(User user, AccessType accessType) {
        UserLimitEntity userLimitEntity = new UserLimitEntity(user.id(), user.userKey(), accessType);
        return userLimitRepository.save(userLimitEntity);
    }
}
