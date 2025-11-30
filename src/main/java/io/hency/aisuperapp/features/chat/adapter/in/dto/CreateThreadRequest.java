package io.hency.aisuperapp.features.chat.adapter.in.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateThreadRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Model name is required")
    private String modelName;

    private Long userId; // Temporary: will be from auth context later
}
