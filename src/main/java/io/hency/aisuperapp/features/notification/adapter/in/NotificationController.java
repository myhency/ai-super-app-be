package io.hency.aisuperapp.features.notification.adapter.in;

import io.hency.aisuperapp.features.notification.adapter.in.dto.CreateNotificationRequest;
import io.hency.aisuperapp.features.notification.adapter.in.dto.CreateNotificationResponse;
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
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationEventService eventService;
    private final CreateNotificationService createNotificationService;

    @PostMapping("/create")
    public Mono<CreateNotificationResponse> create(
            @RequestBody CreateNotificationRequest request
    ) {
        return createNotificationService.createNotification(
                        request.getTitle(),
                        request.getContent(),
                        request.getLocale()
                )
                .doOnSuccess(entity -> log.info("Successfully created notification: {}", entity))
                .doOnError(error -> log.error("Failed to create notification: {}", error.getMessage()))
                .map(notificationEntity -> CreateNotificationResponse.builder()
                        .ulid(notificationEntity.getUlid().toString())
                        .build());
    }

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

                    return ServerSentEvent.builder()
                            .id(event.getUlid())
                            .event(event.getEventType().name())
                            .data(event)
                            .build();
                })
                .doOnNext(sse -> log.info("Sending SSE: id={}, event={}", sse.id(), sse.event()))
                .onErrorContinue((error, obj) -> log.error("Error processing event: {}, object: {}", error.getMessage(), obj, error));

        // 하트비트 이벤트와 병합
        Flux<ServerSentEvent<Object>> heartbeatFlux = Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> {
                    log.debug("Sending heartbeat (seq: {})", sequence);
                    return ServerSentEvent.builder()
                            .comment("heartbeat")
                            .build();
                });

        // 초기 연결 확인 이벤트
        Flux<ServerSentEvent<Object>> initialFlux = Flux.just(
                ServerSentEvent.builder()
                        .event("connection")
                        .data(Map.of("status", "connected", "timestamp", System.currentTimeMillis()))
                        .build()
        ).doOnNext(sse -> log.info("Sending initial connection event"));

        return Flux.merge(initialFlux, eventFlux, heartbeatFlux)
                .doOnSubscribe(subscription -> log.info("SSE connection established: {}", subscription))
                .doOnCancel(() -> log.info("SSE subscription cancelled"))
                .doOnComplete(() -> log.info("SSE subscription completed"))
                .doOnError(error -> log.error("SSE error: {}", error.getMessage(), error));
    }
}