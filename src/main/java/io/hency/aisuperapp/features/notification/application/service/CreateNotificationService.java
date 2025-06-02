package io.hency.aisuperapp.features.notification.application.service;

import com.github.f4b6a3.ulid.UlidCreator;
import io.hency.aisuperapp.features.notification.application.domain.entity.NotificationEntity;
import io.hency.aisuperapp.features.notification.application.domain.vo.NotificationChangeEvent;
import io.hency.aisuperapp.features.notification.infrastructure.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateNotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationEventService eventService;

    @Transactional
    public Mono<NotificationEntity> createNotification(String title, String content, String locale) {
        NotificationEntity notification = new NotificationEntity(
                null,
                UlidCreator.getMonotonicUlid(),
                title,
                content,
                locale
        );

        return notificationRepository.save(notification)
                .doOnSuccess(savedEntity -> {
                    log.info("Notification created, attempting to publish event: {}", savedEntity);
                    try {
                        NotificationChangeEvent event = NotificationChangeEvent.builder()
                                .eventType(NotificationChangeEvent.EventType.CREATED)
                                .ulid(savedEntity.getUlid().toString())
                                .title(savedEntity.getTitle())
                                .content(savedEntity.getContent())
                                .build();

                        eventService.publishEvent(event);
                    } catch (Exception e) {
                        log.error("Failed to publish notification event after creation: {}", e.getMessage(), e);
                    }
                });
    }
}
