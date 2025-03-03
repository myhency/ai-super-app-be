package io.hency.aisuperapp.common.domain.entity;

import com.github.f4b6a3.ulid.Ulid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.ZonedDateTime;

@Getter
@NoArgsConstructor
public abstract class BaseAuditEntity extends BaseDateEntity {
    @CreatedBy
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    private boolean isDeleted = false;
    private String deletedBy = null;
    private ZonedDateTime deletedAt = null;

    public BaseAuditEntity(Ulid userId) {
        this.createdBy = userId.toString();
        this.updatedBy = userId.toString();
    }

    public void delete(Ulid userId) {
        this.isDeleted = true;
        this.deletedBy = userId.toString();
        this.deletedAt = ZonedDateTime.now();
    }
}
