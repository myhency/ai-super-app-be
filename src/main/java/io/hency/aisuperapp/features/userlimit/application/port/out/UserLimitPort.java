package io.hency.aisuperapp.features.userlimit.application.port.out;

import io.hency.aisuperapp.auth.constant.AccessType;
import io.hency.aisuperapp.features.user.domain.entity.User;
import io.hency.aisuperapp.features.userlimit.domain.entity.UserLimitEntity;
import reactor.core.publisher.Mono;

public interface UserLimitPort {
    Mono<UserLimitEntity> upsertUserAccessType(User user, AccessType accessType);
}
