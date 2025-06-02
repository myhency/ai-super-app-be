package io.hency.aisuperapp.features.notification.application.service;

import io.hency.aisuperapp.features.notification.application.domain.vo.NotificationChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class NotificationEventService {
    // Sink 참조를 AtomicReference로 관리하여 필요시 재생성 가능
    private final AtomicReference<Sinks.Many<NotificationChangeEvent>> eventSinkRef;

    public NotificationEventService() {
        // 초기 Sink 생성
        Sinks.Many<NotificationChangeEvent> initialSink = createNewSink();
        this.eventSinkRef = new AtomicReference<>(initialSink);
        log.info("NotificationEventService initialized with multicast sink");
    }

    // 새로운 Sink 생성 메서드
    private Sinks.Many<NotificationChangeEvent> createNewSink() {
        return Sinks.many()
                .multicast()
                .onBackpressureBuffer();
    }

    // 필요시 Sink 재생성 메서드
    private Sinks.Many<NotificationChangeEvent> getOrCreateSink() {
        Sinks.Many<NotificationChangeEvent> currentSink = eventSinkRef.get();

        // 현재 Sink가 취소되었거나 오류 상태인 경우 새로 생성
        if (currentSink == null) {
            Sinks.Many<NotificationChangeEvent> newSink = createNewSink();
            if (eventSinkRef.compareAndSet(null, newSink)) {
                log.info("Created new event sink due to null reference");
                return newSink;
            } else {
                return eventSinkRef.get(); // 다른 스레드가 이미 생성함
            }
        }

        return currentSink;
    }

    public void publishEvent(NotificationChangeEvent event) {
        log.info("Attempting to publish notification event: {}", event);

        // 상세 정보 로그
        log.debug("Event details - type: {}, ulid: {}, title: {}, content: {}",
                event.getEventType(), event.getUlid(), event.getTitle(), event.getContent());

        // 최대 3번 발행 시도
        int maxRetries = 3;
        Sinks.EmitResult result = null;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            Sinks.Many<NotificationChangeEvent> currentSink = getOrCreateSink();
            result = currentSink.tryEmitNext(event);

            if (result.isSuccess()) {
                log.info("Successfully published notification event on attempt {}: {}", attempt + 1, event);
                return;
            } else if (result == Sinks.EmitResult.FAIL_CANCELLED ||
                    result == Sinks.EmitResult.FAIL_TERMINATED) {
                log.warn("Sink is cancelled or terminated, creating new sink. Attempt {}/{}",
                        attempt + 1, maxRetries);
                // Sink를 새로 생성하고 다시 시도
                eventSinkRef.set(createNewSink());
            } else {
                // 다른 오류는 재시도하지 않고 로그만 남김
                log.error("Failed to publish notification event (attempt {}/{}): {}, result: {}",
                        attempt + 1, maxRetries, event, result);
                break;
            }
        }

        // 최종적으로 실패한 경우
        if (result != null && !result.isSuccess()) {
            log.error("Failed to publish notification event after {} attempts: {}, final result: {}",
                    maxRetries, event, result);
        }
    }

    public Flux<NotificationChangeEvent> getEventStream() {
        log.info("Getting event stream from sink");

        // 현재 활성 Sink에서 Flux 가져오기
        Sinks.Many<NotificationChangeEvent> currentSink = getOrCreateSink();

        return currentSink.asFlux()
                .share()
                .onBackpressureBuffer()
                .doOnSubscribe(subscription -> {
                    log.info("New subscription to event stream: {}", subscription);
                })
                .doOnNext(event -> {
                    log.info("Sending event to subscriber: {}", event);
                })
                .doOnCancel(() -> {
                    log.info("Subscription to event stream cancelled");
                })
                .doOnComplete(() -> {
                    log.info("Event stream completed");
                })
                .doOnError(error -> {
                    log.error("Error in event stream: {}", error.getMessage());
                });
    }
}