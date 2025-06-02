package io.hency.aisuperapp.features.notification.adapter.in;

import io.hency.aisuperapp.features.notification.adapter.in.dto.CreateNotificationRequest;
import io.hency.aisuperapp.features.notification.application.domain.vo.NotificationChangeEvent;
import io.hency.aisuperapp.features.notification.application.service.CreateNotificationService;
import io.hency.aisuperapp.features.notification.application.service.NotificationEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/v1/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationEventService eventService;
    private final CreateNotificationService createNotificationService;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Object>> subscribe() {
        log.info("New SSE subscription request received");

        // 이벤트 스트림 변환
        Flux<ServerSentEvent<Object>> eventFlux = eventService.getEventStream()
                .map(event -> {
                    log.info("Converting event to SSE: {}", event);

                    // 이벤트 데이터 로깅
                    log.debug("Event data details - type: {}, ulid: {}, title: {}",
                            event.getEventType(), event.getUlid(), event.getTitle());

                    // Java 객체 복잡성을 줄이기 위해 필요한 데이터만 포함하는 간단한 맵으로 변환
                    // 이렇게 하면 직렬화 문제를 줄일 수 있습니다
                    return ServerSentEvent.builder()
                            .id(event.getUlid().toString())
                            .event(event.getEventType().name())
                            .data(event)
                            .build();
                })
                .doOnNext(sse -> log.info("Sending SSE: id={}, event={}", sse.id(), sse.event()));

        // 하트비트 이벤트와 병합
        Flux<ServerSentEvent<Object>> heartbeatFlux = Flux.interval(Duration.ofSeconds(15))
                .map(sequence -> {
                    log.debug("Sending heartbeat (seq: {})", sequence);
                    return ServerSentEvent.<Object>builder()
                            .comment("heartbeat")
                            .build();
                });

        return Flux.merge(eventFlux, heartbeatFlux)
                .doOnSubscribe(subscription -> log.info("SSE connection established: {}", subscription))
                .doOnCancel(() -> log.info("SSE subscription cancelled"))
                .doOnError(error -> log.error("SSE error: {}", error.getMessage()));
    }

    @PostMapping("/create")
    public Mono<String> testCreate(
            @RequestBody CreateNotificationRequest request
    ) {
        log.info("Received create notification request: {}", request);

        return createNotificationService.createNotification(
                        request.getTitle(),
                        request.getContent(),
                        request.getLocale()
                )
                .doOnSuccess(entity -> log.info("Successfully created notification: {}", entity))
                .doOnError(error -> log.error("Failed to create notification: {}", error.getMessage()))
                .map(notificationEntity -> "Created: " + notificationEntity.getUlid());
    }

    // 문제 해결을 위한 테스트 엔드포인트 추가
    @GetMapping("/test-connection")
    public Mono<String> testConnection() {
        log.info("Testing notification service connection");
        return Mono.just("Notification service is up and running");
    }
}