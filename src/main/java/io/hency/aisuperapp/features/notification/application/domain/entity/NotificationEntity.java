package io.hency.aisuperapp.features.notification.application.domain.entity;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.domain.entity.BaseDateEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@AllArgsConstructor
@Table(name="notification")
public class NotificationEntity extends BaseDateEntity {

    @Id
    private Long id;
    private Ulid ulid;
    private String title;
    private String content;
    private String locale;
}
