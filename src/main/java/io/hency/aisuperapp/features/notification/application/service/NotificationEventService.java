package io.hency.aisuperapp.features.notification.application.service;

import io.hency.aisuperapp.features.notification.application.domain.vo.NotificationChangeEvent;
import io.hency.aisuperapp.features.notification.infrastructure.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
public class NotificationEventService implements DisposableBean {

    private final NotificationRepository notificationRepository;

    // Hot publisher로 변경하여 구독자가 없어도 이벤트를 보관
    private final Sinks.Many<NotificationChangeEvent> eventSink;
    private final Flux<NotificationChangeEvent> eventFlux;

    public NotificationEventService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;

        // replay(1) 사용하여 마지막 이벤트를 새 구독자에게도 전달
        this.eventSink = Sinks.many()
                .replay()
                .limit(1);

        // Hot Flux로 생성하여 구독자 유무와 관계없이 이벤트 스트림 유지
        this.eventFlux = eventSink.asFlux()
                .share() // 여러 구독자가 같은 스트림을 공유
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(subscription -> {
                    log.info("New subscription to event stream: {}", subscription);
                })
                .doOnNext(event -> {
                    log.info("Broadcasting event to all subscribers: {}", event);
                })
                .doOnCancel(() -> {
                    log.info("Subscription to event stream cancelled");
                })
                .doOnComplete(() -> {
                    log.info("Event stream completed");
                })
                .doOnError(error -> {
                    log.error("Error in event stream: {}", error.getMessage(), error);
                });

        log.info("NotificationEventService initialized with hot multicast sink");
    }

    public void publishEvent(NotificationChangeEvent event) {
        log.info("Publishing notification event: {}", event);

        try {
            Sinks.EmitResult result = eventSink.tryEmitNext(event);

            switch (result) {
                case OK:
                    log.info("Successfully published notification event: {}", event);
                    break;
                case FAIL_ZERO_SUBSCRIBER:
                    log.warn("No subscribers for event, but event is buffered: {}", event);
                    // Hot publisher이므로 구독자가 없어도 버퍼에 저장됨
                    break;
                case FAIL_OVERFLOW:
                    log.error("Buffer overflow when publishing event: {}", event);
                    break;
                case FAIL_CANCELLED:
                case FAIL_TERMINATED:
                    log.error("Sink is cancelled or terminated, cannot publish event: {}", event);
                    break;
                case FAIL_NON_SERIALIZED:
                    log.error("Non-serialized access to sink when publishing event: {}", event);
                    break;
                default:
                    log.error("Unknown emit result when publishing event: {}, result: {}", event, result);
            }
        } catch (Exception e) {
            log.error("Exception occurred while publishing event: {}", event, e);
        }
    }

    public Flux<NotificationChangeEvent> getEventStream() {
        log.info("Providing event stream to new subscriber");
        return eventFlux;
    }

    /**
     * 클라이언트 연결 시 최신 알림을 조회하여 이벤트로 변환하여 반환
     */
    public Mono<NotificationChangeEvent> getLatestNotificationAndPublish() {
        log.info("Fetching latest notification for new subscriber");

        return notificationRepository.findLatestNotification()
                .map(notification -> {
                    log.info("Found latest notification: {}", notification);
                    return NotificationChangeEvent.builder()
                            .eventType(NotificationChangeEvent.EventType.LATEST)
                            .ulid(notification.getUlid().toString())
                            .title(notification.getTitle())
                            .content(notification.getContent())
                            .build();
                })
                .doOnNext(event -> log.info("Created latest notification event: {}", event))
                .doOnError(error -> log.error("Failed to fetch latest notification: {}", error.getMessage(), error))
                .onErrorReturn(NotificationChangeEvent.builder()
                        .eventType(NotificationChangeEvent.EventType.LATEST)
                        .ulid("")
                        .title("No notifications available")
                        .content("")
                        .build());
    }

    @Override
    public void destroy() {
        log.info("Cleaning up NotificationEventService");
        try {
            eventSink.tryEmitComplete();
        } catch (Exception e) {
            log.error("Error during cleanup: {}", e.getMessage(), e);
        }
    }
}