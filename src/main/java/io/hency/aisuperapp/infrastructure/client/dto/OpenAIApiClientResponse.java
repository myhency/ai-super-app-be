package io.hency.aisuperapp.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIApiClientResponse {
    private List<Choice> choices;
    private long created;
    private String id;
    private String model;
    private Usage usage;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Choice {
        @JsonProperty("content_filter_results")
        private ContentFilterResults contentFilterResults;
        private Delta delta;
        @JsonProperty("finish_reason")
        private String finishReason;
        private int index;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContentFilterResults {
        private FilterResult hate;
        @JsonProperty("self_harm")
        private FilterResult selfHarm;
        private FilterResult sexual;
        private FilterResult violence;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FilterResult {
        private boolean filtered;
        private String severity;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Delta {
        private String content;
        private String refusal;
        private String role;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Usage {
        @JsonProperty("completion_tokens")
        private int completionTokens;
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;
    }
}
