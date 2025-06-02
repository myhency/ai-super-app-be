package io.hency.aisuperapp.features.notification.infrastructure.repository;

import io.hency.aisuperapp.features.notification.application.domain.entity.NotificationEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface NotificationRepository  extends R2dbcRepository<NotificationEntity, Long> {
    @Query("SELECT * FROM notification ORDER BY id DESC LIMIT 1")
    Mono<NotificationEntity> findLatestNotification();
}
