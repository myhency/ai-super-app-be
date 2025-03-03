package io.hency.aisuperapp.features.chat.domain.entity;

import io.hency.aisuperapp.features.chat.domain.enums.ChatRoleType;

public record Message(
        String content,
        ChatRoleType role
) {
    public static Message userMessage(String content) {
        return new Message(content, ChatRoleType.USER);
    }

    public static Message aiMessage(String content) {
        return new Message(content, ChatRoleType.AI);
    }

    public static Message systemMessage(String content) {
        return new Message(content, ChatRoleType.SYSTEM);
    }
}
