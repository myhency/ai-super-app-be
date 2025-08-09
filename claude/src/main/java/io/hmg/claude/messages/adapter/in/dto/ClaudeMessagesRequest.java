package io.hmg.claude.messages.adapter.in.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.hmg.claude.common.adapter.in.dto.BaseRequest;
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

    @NotEmpty(message = "messages should not be empty")
    private List<Message> messages;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    @NotNull(message = "model cannot be null.")
    private String model;

    private Object metadata;

    @JsonProperty("stop_sequences")
    private List<String> stopSequences;

    private Boolean stream = false;

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
        @JsonDeserialize(using = ContentDeserializer.class)
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

        private Object source;

        private String mediaType;

        private String id;

        @JsonUnwrapped
        private ToolUse toolUse;

        @JsonProperty("content")
        private String resultContent;

        @JsonProperty("tool_use_id")
        private String toolUseId;

        @JsonProperty("cache_control")
        private CacheControl cacheControl;

        private List<Citation> citations;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ImageSource {

        private String type;

        @JsonProperty("media_type")
        private String mediaType;

        private String data;


        @JsonProperty("file_id")
        private String fileId;

        private Object content;

        // Getter methods
        public String getType() {
            return type;
        }

        public String getMediaType() {
            // base64 타입일 때 기본값 설정
            if ("base64".equals(type) && mediaType == null) {
                return "image/png";
            }
            return mediaType;
        }

        public String getData() {
            return data;
        }


        public String getFileId() {
            return fileId;
        }

        public Object getContent() {
            return content;
        }

        // Setter methods
        public void setType(String type) {
            this.type = type;
        }

        public void setMediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public void setData(String data) {
            this.data = data;
        }


        public void setFileId(String fileId) {
            this.fileId = fileId;
        }

        public void setContent(Object content) {
            this.content = content;
        }

        // equals, hashCode, toString
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ImageSource)) return false;
            ImageSource that = (ImageSource) o;
            return java.util.Objects.equals(type, that.type) &&
                   java.util.Objects.equals(mediaType, that.mediaType) &&
                   java.util.Objects.equals(data, that.data) &&
                   java.util.Objects.equals(fileId, that.fileId) &&
                   java.util.Objects.equals(content, that.content);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(type, mediaType, data, fileId, content);
        }

        @Override
        public String toString() {
            return "ImageSource{" +
                   "type='" + type + '\'' +
                   ", mediaType='" + mediaType + '\'' +
                   ", data='" + data + '\'' +
                   ", fileId='" + fileId + '\'' +
                   ", content=" + content +
                   '}';
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DocumentSource {

        private String type;

        @JsonProperty("media_type")
        private String mediaType;

        private String data;

        @JsonProperty("file_id")
        private String fileId;

        private Object content;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ThinkingSource {

        private String type;

        private String signature;

        private String thinking;
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CacheControl {

        private String type;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Citation {

        @JsonProperty("cited_text")
        private String citedText;

        @JsonProperty("document_index")
        private Integer documentIndex;

        @JsonProperty("document_title")
        private String documentTitle;

        @JsonProperty("end_char_index")
        private Integer endCharIndex;

        @JsonProperty("start_char_index")
        private Integer startCharIndex;

        private String type;
    }



    public static class ContentDeserializer extends JsonDeserializer<Object> {
        @Override
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonToken token = p.getCurrentToken();
            
            if (token == JsonToken.VALUE_STRING) {
                return p.getValueAsString();
            } else if (token == JsonToken.START_ARRAY) {
                JsonNode node = p.readValueAsTree();
                return ctxt.readTreeAsValue(node, TypeFactory.defaultInstance().constructCollectionType(List.class, ContentBlock.class));
            } else if (token == JsonToken.START_OBJECT) {
                JsonNode node = p.readValueAsTree();
                return ctxt.readTreeAsValue(node, ContentBlock.class);
            }
            
            return null;
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
