package io.hency.aisuperapp.features.user.domain.entity;

import java.time.ZonedDateTime;

public record User(
        String id,
        String tenantId,
        String userKey,
        String userName,
        String email,
        String subscribeId,
        ZonedDateTime lastAccessedAt,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {

    public static User of(UserEntity userEntity) {
        return new User(
                userEntity.getUlid().toString(),
                userEntity.getTenantId(),
                userEntity.getUserKey(),
                userEntity.getUserName(),
                userEntity.getEmail(),
                userEntity.getSubscribeId(),
                userEntity.getLastAccessedAt(),
                userEntity.getCreatedAt(),
                userEntity.getUpdatedAt()
        );
    }
}
