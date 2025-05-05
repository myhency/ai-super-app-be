package io.hency.aisuperapp.features.userlimit.application.port.out;

import io.hency.aisuperapp.auth.application.domain.vo.AccessType;
import io.hency.aisuperapp.features.user.application.domain.entity.User;
import io.hency.aisuperapp.features.userlimit.application.domain.entity.UserLimitEntity;
import reactor.core.publisher.Mono;

public interface UserLimitPort {
    Mono<UserLimitEntity> upsertUserAccessType(User user, AccessType accessType);
}
