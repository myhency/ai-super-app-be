package io.hmg.openai.chat.completion.adapter.in.dto;

import com.fasterxml.jackson.annotation.*;
import io.hmg.openai.common.adapter.in.dto.BaseRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionRequest extends BaseRequest {
    @NotEmpty(message = "messages should not be empty")
    private List<Message> messages;

    private String model;

    private Object audio;

    @JsonProperty("frequency_penalty")
    private Integer frequencyPenalty;

    @JsonProperty("function_call")
    private String functionCall;

    private List<Object> functions;

    @JsonProperty("logit_bias")
    private Object logitBias;

    private Boolean logprobs;

    @JsonProperty("max_completion_tokens")
    private Integer maxCompletionTokens;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    private Object metadata;

    private List<Object> modalities;

    private Integer n;

    @JsonProperty("parallel_tool_calls")
    private Boolean parallelToolCalls;

    private Object prediction;

    @JsonProperty("presence_penalty")
    private Integer presencePenalty;

    @JsonProperty("reasoning_effort")
    private String reasoningEffort;

    @JsonProperty("response_format")
    private Object responseFormat;

    private Integer seed;

    private String stop;

    private Boolean stream = false;

    @JsonProperty("stream_options")
    private StreamOptions streamOptions;

    private Double temperature;

    @JsonProperty("tool_choice")
    private String toolChoice;

    private List<Object> tools;

    @JsonProperty("top_logprobs")
    private Integer topLogprobs;

    @JsonProperty("top_p")
    private Double topP;

    private String user;

    @JsonProperty("web_search_options")
    private Object webSearchOptions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {

        private String name;

        private String role;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        private Object content;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        @JsonIgnoreProperties(ignoreUnknown = true)
        private Object audio;

        private String refusal;

        private List<Object> toolCalls;

        @JsonProperty("tool_call_id")
        private String toolCallId;
    }

    @Data
    @AllArgsConstructor
    public static class StreamOptions {

        @JsonProperty("include_usage")
        private Boolean includeUsage;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}