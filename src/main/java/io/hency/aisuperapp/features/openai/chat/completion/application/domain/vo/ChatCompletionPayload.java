package io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionPayload {
    private Double temperature;
    private Double topP;
    private Boolean stream = false;
    private List<Message> messages;
    private StreamOptions streamOptions;
    private List<Tool> tools;
}