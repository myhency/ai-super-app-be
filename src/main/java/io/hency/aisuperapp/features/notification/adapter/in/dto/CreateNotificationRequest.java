package io.hency.aisuperapp.features.notification.adapter.in.dto;

import lombok.Data;

@Data
public class CreateNotificationRequest {
    private String title;
    private String content;
    private String locale;
}
