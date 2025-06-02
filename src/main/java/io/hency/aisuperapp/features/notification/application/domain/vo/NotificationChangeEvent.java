package io.hency.aisuperapp.features.notification.application.domain.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationChangeEvent {
    private EventType eventType;
    private String ulid;
    private String title;
    private String content;

    public enum EventType {
        CREATED,    // 새로운 알림이 생성됨
        UPDATED,    // 알림이 업데이트됨
        LATEST      // 최신 알림 (클라이언트 연결 시)
    }
}