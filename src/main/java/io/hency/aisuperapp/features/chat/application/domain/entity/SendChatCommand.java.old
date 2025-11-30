package io.hency.aisuperapp.features.chat.application.domain.entity;

import com.github.f4b6a3.ulid.Ulid;

import java.util.List;

public record SendChatCommand(
        String tenantId,
        Ulid topicId,
        Ulid chatId,
        Ulid aiChatId,
        Ulid userId,
        Ulid parentChatId,
        List<Message> previousMessages,
        String content
) {
}
