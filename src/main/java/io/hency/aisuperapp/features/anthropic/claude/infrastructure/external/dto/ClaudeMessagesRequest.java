package io.hency.aisuperapp.features.anthropic.claude.infrastructure.external.dto;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.hency.aisuperapp.features.openai.chat.completion.adapter.in.dto.ChatCompletionRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeMessagesRequest {
    private Integer maxTokens;
    @NotEmpty(message = "messages should not be empty")
    private List<Message> messages;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @NoArgsConstructor
    public static class Message {
        @JsonProperty("role")
        private Role role;

        @JsonProperty("content")
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonUnwrapped
        private MessageContent content;

        public interface MessageContent {}

        public record TextMessageContent(
                @JsonValue
                String content
        ) implements Message.MessageContent {}

        public record MultiPartMessageContent(List<Message.Content> content)
                implements Message.MessageContent {}

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY)
        @JsonSubTypes({
                @JsonSubTypes.Type(value = Message.ContentText.class, name = "text")
//                @JsonSubTypes.Type(value = Message.ContentImageUrl.class, name = "image_url")
        })
        @JsonIgnoreProperties()
        public static abstract class Content {

            @JsonProperty("type")
            protected final Message.Type type;

            protected Content(Message.Type type) { this.type = type;}
        }

        @EqualsAndHashCode(callSuper = true)
        @JsonIgnoreProperties()
        public static class ContentText extends Message.Content {

            @JsonProperty("text")
            private String text;

            public ContentText() { super(Message.Type.text); }

            public ContentText(String text) {
                super(Message.Type.text);
                this.text = text;
            }
        }

        public enum Role {
            user, assistant;

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
            text, image
        }
    }
}
