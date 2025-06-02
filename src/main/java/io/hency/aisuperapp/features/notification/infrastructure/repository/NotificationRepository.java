package io.hency.aisuperapp.features.notification.infrastructure.repository;

import io.hency.aisuperapp.features.notification.application.domain.entity.NotificationEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface NotificationRepository  extends R2dbcRepository<NotificationEntity, Long> {
}
