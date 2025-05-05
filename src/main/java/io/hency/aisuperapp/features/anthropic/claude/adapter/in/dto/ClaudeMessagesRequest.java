package io.hency.aisuperapp.features.anthropic.claude.adapter.in.dto;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.hency.aisuperapp.common.adapter.in.dto.BaseRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeMessagesRequest extends BaseRequest {

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @NotEmpty(message = "messages should not be empty")
    private List<Message> messages;

    @NotNull(message = "model cannot be null.")
    private String model;

    private Object metadata;

    @JsonProperty("stop_sequences")
    private List<String> stopSequences;

    private Boolean stream;

    private Object system;

    private Double temperature;

    private Object thinking;

    private Reasoning reasoning;

    @JsonProperty("tool_choice")
    private ToolChoice toolChoice;

    private List<Tool> tools;

    @JsonProperty("top_k")
    private Integer topK;

    @JsonProperty("top_p")
    private Integer topP;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Message {

        private String role;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Object content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContentBlock {

        private String type;

        private String text;

        private ImageSource source;

        private String mediaType;

        private String id;

        @JsonUnwrapped
        private ToolUse toolUse;

        @JsonProperty("content")
        private String resultContent;

        @JsonProperty("tool_use_id")
        private String toolUseId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonSerialize(using = ImageSourceSerializer.class)
    public static class ImageSource {

        private String type;

        @JsonProperty("media_type")
        private String mediaType;

        private String data;

        private String url;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Reasoning {

        private boolean enabled;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ToolChoice {

        private String type;

        private ToolUse tool;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ToolUse {

        private String id;

        private String name;

        private Map<String, Object> input;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Tool {

        private String name;

        private String description;

        @JsonProperty("input_schema")
        private Map<String, Object> inputSchema;
    }

    public static class ImageSourceSerializer extends JsonSerializer<ImageSource> {
        @Override
        public void serialize(ImageSource value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();

            if (value.getType() != null) {
                gen.writeStringField("type", value.getType());

                if ("base64".equals(value.getType())) {
                    String mediaType = value.getMediaType() != null ? value.getMediaType() : "image/png";
                    gen.writeStringField("media_type", mediaType);
                } else if (value.getMediaType() != null) {
                    gen.writeStringField("media_type", value.getMediaType());
                }
            }

            if (value.getData() != null) {
                gen.writeStringField("data", value.getData());
            }

            if (value.getUrl() != null) {
                gen.writeStringField("url", value.getUrl());
            }

            gen.writeEndObject();
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
