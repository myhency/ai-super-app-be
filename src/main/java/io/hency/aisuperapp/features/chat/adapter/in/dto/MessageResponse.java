package io.hency.aisuperapp.features.chat.adapter.in.dto;

import io.hency.aisuperapp.features.chat.application.domain.entity.Message;
import io.hency.aisuperapp.features.chat.application.domain.vo.MessageRole;
import io.hency.aisuperapp.features.file.application.domain.entity.FileAttachment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
    private List<FileAttachmentInfo> attachments;

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

    public static MessageResponse from(Message message, List<FileAttachment> attachments) {
        return MessageResponse.builder()
                .id(message.getId())
                .threadId(message.getThreadId())
                .role(message.getRole())
                .content(message.getContent())
                .tokenCount(message.getTokenCount())
                .createdAt(message.getCreatedAt())
                .attachments(attachments.stream()
                        .map(FileAttachmentInfo::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
