package io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ChatCompletionPayload {
    @JsonProperty("temperature")
    private Double temperature;

    @JsonProperty("top_p")
    private Double topP;

    @JsonProperty("stream")
    private Boolean stream = false;

    @JsonProperty("messages")
    private List<Message> messages;

    @JsonProperty("stream_options")
    private StreamOptions streamOptions;

    @JsonProperty("tools")
    private List<Tool> tools;

    @JsonProperty("tool_choice")
    private String toolChoice;

    @JsonProperty("function_call")
    private String functionCall;

    @JsonProperty("functions")
    private List<String> functions;
}