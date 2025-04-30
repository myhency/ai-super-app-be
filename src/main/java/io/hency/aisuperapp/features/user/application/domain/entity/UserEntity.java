package io.hency.aisuperapp.features.user.application.domain.entity;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import io.hency.aisuperapp.common.domain.entity.BaseDateEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Getter
@Table(name = "chat_user")
public class UserEntity extends BaseDateEntity {
    @Id
    private Long id;
    private Ulid ulid = UlidCreator.getMonotonicUlid();
    private String tenantId;
    private String userKey;
    private String userName;
    private String email;
    private String subscribeId;
    private ZonedDateTime lastAccessedAt;
    private String createdBy;
    private String updatedBy;
    private boolean isDeleted = false;
    private String deletedBy = null;
    private ZonedDateTime deletedAt = null;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    private UserEntity(Ulid ulid, String tenantId, String userKey, String userName, String email, ZonedDateTime lastAccessedAt, String subscribeId) {
        this.ulid = ulid;
        this.tenantId = tenantId;
        this.userKey = userKey;
        this.userName = userName;
        this.email = email;
        this.lastAccessedAt = lastAccessedAt;
        this.createdBy = ulid.toString();
        this.updatedBy = ulid.toString();
        ZonedDateTime now = ZonedDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        this.subscribeId = subscribeId;
    }

    public static UserEntity of(String tenantId, String userKey, String userName, String email, String subscribeId) {
        return new UserEntity(
                UlidCreator.getMonotonicUlid(),
                tenantId,
                userKey,
                userName,
                email,
                ZonedDateTime.now(),
                subscribeId
        );
    }

    public void delete(Ulid userId) {
        this.isDeleted = true;
        this.deletedBy = userId.toString();
        this.deletedAt = ZonedDateTime.now();
        this.updatedBy = userId.toString();
        updateTime();
    }

    public void updateLastAccessedAt(Ulid userId) {
        this.lastAccessedAt = ZonedDateTime.now();
        this.updatedBy = userId.toString();
        updateTime();
    }
}
