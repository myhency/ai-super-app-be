package io.hency.aisuperapp.features.chat.adapter.in.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreamChunkResponse {

    private String type;
    private Long id;
    private Long threadId;
    private MessageRole role;
    private String content;
    private Integer tokenCount;
    private LocalDateTime createdAt;

    public static StreamChunkResponse chunk(String content) {
        return StreamChunkResponse.builder()
                .type("chunk")
                .role(MessageRole.assistant)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static StreamChunkResponse done() {
        return StreamChunkResponse.builder()
                .type("done")
                .build();
    }
}
