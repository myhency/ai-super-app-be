package io.hency.aisuperapp.features.openai.chat.completion.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatCompletionResponse {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choice> choices;
    private Usage usage;
    private List<PromptFilterResult> prompt_filter_results;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Choice {
        private Delta delta;
        private Message message;
        private String finish_reason;

        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Delta {
            private String content;
            private String role;
            private List<ToolCall> tool_calls;
        }

        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Message {
            private String content;
            private String role;
            private List<ToolCall> tool_calls;
        }

        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ToolCall {
            private String id;
            private String type;
            private Function function;
            private String index;
            private Function delta;
        }

        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Function {
            private String name;
            private String arguments;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        private int total_tokens;
        private int prompt_tokens;
        private int completion_tokens;
        private CompletionTokensDetails completion_tokens_details;
        private PromptTokensDetails prompt_tokens_details;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class PromptTokensDetails {
            private int audio_tokens;
            private int cached_tokens;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class CompletionTokensDetails {
            private int accepted_prediction_tokens;
            private int audio_tokens;
            private int reasoning_tokens;
            private int rejected_prediction_tokens;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PromptFilterResult {
        private int prompt_index;
        private ContentFilterResults content_filter_results;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ContentFilterResults {
            private ContentFilterResult hate;
            private ContentFilterResult jailbreak;
            private ContentFilterResult self_harm;
            private ContentFilterResult sexual;
            private ContentFilterResult violence;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentFilterResult {
        private Boolean filtered;
        private String severity;
        private Boolean detected;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error("ChatCompletionResponse toString() failed.", e);
            return "ChatCompletionResponse toString() failed." + e.getMessage();
        }
    }
}
