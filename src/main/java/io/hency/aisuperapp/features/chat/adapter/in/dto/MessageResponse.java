package io.hency.aisuperapp.features.chat.adapter.in.dto;

import io.hency.aisuperapp.features.chat.application.domain.entity.Message;
import io.hency.aisuperapp.features.chat.application.domain.vo.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private Long threadId;
    private MessageRole role;
    private String content;
    private Integer tokenCount;
    private LocalDateTime createdAt;

    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .threadId(message.getThreadId())
                .role(message.getRole())
                .content(message.getContent())
                .tokenCount(message.getTokenCount())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
