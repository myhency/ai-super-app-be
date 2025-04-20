package io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.hency.aisuperapp.features.openai.chat.completion.adapter.in.dto.ChatCompletionRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class Message {

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
        public static Role fromValue(String value) {
            for (Role role : Role.values()) {
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