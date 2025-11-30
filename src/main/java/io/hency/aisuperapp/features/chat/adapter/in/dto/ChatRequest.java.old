package io.hency.aisuperapp.features.chat.adapter.in.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.util.UlidUtils;
import io.hency.aisuperapp.features.chat.application.domain.entity.Message;
import io.hency.aisuperapp.features.chat.application.domain.entity.SendChatCommand;
import io.hency.aisuperapp.features.chat.application.domain.enums.ChatRoleType;
import io.hency.aisuperapp.features.chat.infrastructure.external.dto.OpenAIApiClientRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChatRequest {
    public record SendMessageRequest(
            @NotNull(message = "userChatId must not be null")
            @NotBlank(message = "userChatId must not be empty")
            String userChatId,

            @NotNull(message = "aiChatId must not be null")
            @NotBlank(message = "aiChatId must not be empty")
            String aiChatId,
            String topicId,
            String parentId,
            List<MessageRequest> previousMessages,
            String content,
            List<Content> contents
    ) {
        public SendMessageRequest {
            if (Objects.isNull(previousMessages)) previousMessages = Collections.emptyList();
        }

        public SendChatCommand toCommand(Ulid topicId, Ulid userId, String tenantId) {
            Ulid userChatUlid = UlidUtils.of(userChatId);
            Ulid aiChatUlid = UlidUtils.of(aiChatId);
            Ulid parentChatUlid = UlidUtils.of(parentId);
            List<Message> messages = previousMessages.stream().map(MessageRequest::toMessage).toList();
            return new SendChatCommand(tenantId, topicId, userChatUlid, aiChatUlid, userId, parentChatUlid, messages, this.content);
        }

        public Ulid topicUlid() {
            return UlidUtils.of(topicId);
        }
    }

    public record ReSendMessageRequest(
            @NotNull(message = "aiChatId must not be null")
            @NotBlank(message = "aiChatId must not be empty")
            String aiChatId,
            List<MessageRequest> previousMessages
    ) {
        public ReSendMessageRequest {
            if (Objects.isNull(previousMessages)) previousMessages = Collections.emptyList();
        }

        public Ulid aiChatUlid() {
            return UlidUtils.of(aiChatId);
        }

        public List<Message> toMessages() {
            return this.previousMessages.stream().map(MessageRequest::toMessage).toList();
        }
    }

    public static class Content {
        private String type;
        private String text;
        @JsonProperty("image_url")
        private OpenAIApiClientRequest.ChatRequestMessage.Content.ImageUrl imageUrl;

        @Data
        @Builder
        public static class ImageUrl {
            private String url;
        }
    }

    public record MessageRequest(
            RoleTypeRequest role,
            String content
    ) {
        public Message toMessage() {
            return new Message(content, role.toRoleType());
        }
    }

    public enum RoleTypeRequest {
        AI, USER;

        public ChatRoleType toRoleType() {
            return switch (this) {
                case AI -> ChatRoleType.AI;
                case USER -> ChatRoleType.USER;
            };
        }
    }
}
