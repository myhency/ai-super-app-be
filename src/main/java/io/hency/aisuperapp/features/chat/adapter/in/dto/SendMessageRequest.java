package io.hency.aisuperapp.features.chat.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotNull(message = "Thread ID is required")
    private Long threadId;

    @NotBlank(message = "Content is required")
    private String content;

    private Boolean stream = false;

    private List<Long> fileIds;
}
