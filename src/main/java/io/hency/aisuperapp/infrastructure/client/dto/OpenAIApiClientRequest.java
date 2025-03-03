package io.hency.aisuperapp.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

// https://learn.microsoft.com/en-us/azure/ai-services/openai/gpt-v-quickstart?tabs=command-line%2Ckeyless%2Cjavascript-keyless%2Ctypescript-keyless&pivots=rest-api#create-a-new-python-application
// https://learn.microsoft.com/ko-kr/azure/ai-services/openai/how-to/gpt-with-vision?tabs=rest#detail-parameter-settings-in-image-processing-low-high-auto
@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenAIApiClientRequest {
    @Builder.Default
    private List<ChatRequestMessage> messages = new ArrayList<>();
    private boolean stream;
    private double temperature;
    @JsonProperty("top_p")
    private double topP;
    @JsonProperty("max_tokens")
    private int maxTokens;

    @Data
    @Builder
    public static class ChatRequestMessage {
        private String role;
        private List<Content> content;

        @Data
        @Builder
        public static class Content {
            private String type;
            private String text;
            @JsonProperty("image_url")
            private ImageUrl imageUrl;

            @Data
            @Builder
            public static class ImageUrl {
                private String url;
            }
        }
    }
}