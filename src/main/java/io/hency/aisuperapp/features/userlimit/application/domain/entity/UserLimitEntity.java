package io.hency.aisuperapp.features.userlimit.application.domain.entity;

// import io.hency.aisuperapp.auth.application.domain.vo.AccessType; // Removed with auth module
import io.hency.aisuperapp.common.domain.entity.BaseDateEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Getter
@Table(name = "user_limit")
public class UserLimitEntity extends BaseDateEntity {
    @Id
    private Long id;
    private String ulid;
    private String userKey;
    private ZonedDateTime sendChatExpiry;
    @Setter
    private String accessType; // Changed from AccessType enum (removed with auth module)

    public UserLimitEntity(String ulid, String userKey) {
        this.ulid = ulid;
        this.userKey = userKey;
    }

    public UserLimitEntity(String ulid, String userKey, String accessType)  {
        this.ulid = ulid;
        this.userKey = userKey;
        this.accessType = accessType;
    }

    public UserLimitEntity(UserLimitEntity userLimitEntity, String accessType) {
        this.ulid = userLimitEntity.getUlid();
        this.userKey = userLimitEntity.getUserKey();
        this.sendChatExpiry = userLimitEntity.getSendChatExpiry();
        this.accessType = accessType;
    }
}
