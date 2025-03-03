package io.hency.aisuperapp.common.domain.entity;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public abstract class BaseIdEntity extends BaseAuditEntity {
    @Id
    private Long id;

    private Ulid ulid = UlidCreator.getMonotonicUlid();

    public BaseIdEntity(Ulid ulid, Ulid userId) {
        super(userId);
        this.ulid = ulid;
    }
}
