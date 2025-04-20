package io.hency.aisuperapp.features.openai.chat.completion.adapter.in.dto;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo.ChatCompletionModel;
import io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo.GptSeriesPayload;
import io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo.OSeriesPayload;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatCompletionRequest {
    private Double temperature;
    private Double topP;
    private Boolean stream = false;
    private Integer maxTokens;
    private Integer maxCompletionTokens;
    @NotEmpty(message = "messages should not be empty")
    private List<Message> messages;
    private StreamOptions streamOptions;
    private List<Tool> tools;
    private String toolChoice;
    private String functionCall;
    private List<String> functions;

    @Data
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    public static class Message {

        @JsonProperty("role")
        private Role role;

        @JsonProperty("content")
        @JsonDeserialize(using = MessageContentDeserializer.class)
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonUnwrapped
        private MessageContent content;

        public Message(String role, String content) {
            this.role = Role.valueOf(role);
            this.content = new TextMessageContent(content);
        }

        public interface MessageContent {}

        public record TextMessageContent(
                @JsonValue
                String content
        ) implements MessageContent {}

        public record MultiPartMessageContent(List<Content> content)
                implements MessageContent {}

        public static class MessageContentDeserializer extends JsonDeserializer<MessageContent> {

            @Override
            public MessageContent deserialize(JsonParser p, DeserializationContext ctxt)
                    throws IOException, JacksonException {
                JsonNode node = p.getCodec().readTree(p);
                if (node.isTextual()) {
                    return new TextMessageContent(node.asText());
                } else if (node.isArray()) {
                    ObjectMapper mapper = (ObjectMapper) p.getCodec();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

                    if (node.isEmpty()) {
                        throw new JsonParseException(p, "Content array cannot be empty");
                    }

                    List<Content> contents = mapper.convertValue(node, new TypeReference<>() {
                    });
                    return new MultiPartMessageContent(contents);
                } else if (node.isObject()) {
                    ObjectMapper mapper = (ObjectMapper) p.getCodec();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

                    Content content = mapper.convertValue(node, Content.class);
                    return new MultiPartMessageContent(Collections.singletonList(content));
                }
                throw new JsonParseException(p, "Unexpected content format");
            }
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
        @JsonSubTypes({
                @JsonSubTypes.Type(value = ContentText.class, name = "text"),
                @JsonSubTypes.Type(value = ContentImageUrl.class, name = "image_url")
        })
        @JsonIgnoreProperties()
        public static abstract class Content {

            @JsonProperty("type")
            protected final Type type;

            protected Content(Type type) { this.type = type;}
        }

        @EqualsAndHashCode(callSuper = true)
        @JsonIgnoreProperties()
        public static class ContentText extends Content {

            @JsonProperty("text")
            private String text;

            public ContentText() { super(Type.text); }

            public ContentText(String text) {
                super(Type.text);
                this.text = text;
            }
        }

        @EqualsAndHashCode(callSuper = true)
        @JsonIgnoreProperties()
        public static class ContentImageUrl extends Content {

            @JsonProperty("image_url")
            private ImageUrl imageUrl;

            public ContentImageUrl() { super(Type.image_url); }

            public ContentImageUrl(ImageUrl imageUrl) {
                super(Type.image_url);
                this.imageUrl = imageUrl;
            }
        }

        @JsonIgnoreProperties()
        public static class ImageUrl {

            @JsonProperty("url")
            private String url;

            public ImageUrl() {}

            public ImageUrl(String url) { this.url = url; }
        }

        public enum Role {
            system, user, assistant, tool;

            @JsonCreator
            public static Message.Role fromValue(String value) {
                for (Message.Role role : Message.Role.values()) {
                    if (role.name().equalsIgnoreCase(value)) {
                        return role;
                    }
                }

                throw new RuntimeException();
            }

            @JsonValue
            public String toValue() { return this.name(); }
        }

        public enum Type {
            text, image_url
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Tool {
        private String type;
        private FunctionDefinition function;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FunctionDefinition {
        private String name;
        private String description;
        private JsonSchema parameters;
        private Boolean strict;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class JsonSchema {
        private String type;
        private Map<String, JsonSchemaProperty> properties;
        private List<String> required;
        private Boolean additionalProperties;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class JsonSchemaProperty {
        private Object type;
        private String description;
        private Map<String, JsonSchemaProperty> properties;
        private List<String> required;
        private Boolean additionalProperties;
        private List<String> enumValues;
    }

    public static class StreamOptions {
        private Boolean includeUsage;
    }

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return super.toString();
        }
    }

    public OSeriesPayload toOSeriesPayload(ChatCompletionModel model) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String jsonString = mapper.writeValueAsString(this);

            OSeriesPayload payload = mapper.readValue(jsonString, OSeriesPayload.class);

            if (this.maxCompletionTokens != null) {
                payload.setMaxCompletionTokens(this.maxCompletionTokens);
            }

            if (this.maxTokens != null) {
                throw new RuntimeException();
            }

            if(model.equals(ChatCompletionModel.O3)) {
                payload.setTemperature(1.0);
            }

            return payload;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public GptSeriesPayload toGptSeriesPayload() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            String jsonString = mapper.writeValueAsString(this);

            GptSeriesPayload payload = mapper.readValue(jsonString, GptSeriesPayload.class);

            if (this.maxCompletionTokens != null) {
                throw new RuntimeException();

            }

            if (this.maxTokens != null) {
                payload.setMaxTokens(this.maxTokens);
            }

            return payload;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
