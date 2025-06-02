package io.hency.aisuperapp.features.notification.application.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationChangeEvent {

    private EventType eventType;
    private String ulid;
    private String title;
    private String content;

    public enum EventType {
        CREATED, UPDATED
    }
}
