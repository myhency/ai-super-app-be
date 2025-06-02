package io.hency.aisuperapp.features.notification.adapter.in;

import com.github.f4b6a3.ulid.UlidCreator;
import io.hency.aisuperapp.features.notification.application.domain.vo.NotificationChangeEvent;
import io.hency.aisuperapp.features.notification.application.service.NotificationEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/v1/notification/test")
@RequiredArgsConstructor
public class TextNotificationEndpoint {

    private final NotificationEventService notificationEventService;

    @GetMapping("/emit-test-event")
    public Mono<String> emitTestEvent() {
        log.info("Manual test event emission requested");

        // 테스트 이벤트 생성
        NotificationChangeEvent testEvent = createTestEvent();
        log.info("Created test event: {}", testEvent);

        // 이벤트 발행
        notificationEventService.publishEvent(testEvent);

        return Mono.just("Test event emitted: " + testEvent.getUlid());
    }

    private NotificationChangeEvent createTestEvent() {
        // 이 부분은 실제 NotificationChangeEvent 구현에 맞게 수정해야 합니다
        // 아래는 예시 코드로, 실제 구현체에 맞게 변경하세요
        return NotificationChangeEvent.builder()
                .eventType(NotificationChangeEvent.EventType.CREATED)
                .ulid(UlidCreator.getMonotonicUlid().toString())  // 실제 ULID 생성 방식으로 변경
                .title("Test Event " + LocalDateTime.now())
                .content("This is a manual test event triggered at " + LocalDateTime.now())
                .build();
    }
}
