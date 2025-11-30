package io.hency.aisuperapp.common.infrastructure.config.database.r2dbc.converter.read;

import io.hency.aisuperapp.features.chat.application.domain.vo.MessageRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class MessageRoleReadConverter implements Converter<String, MessageRole> {
    @Override
    public MessageRole convert(String source) {
        return switch (source.toLowerCase()) {
            case "user" -> MessageRole.user;
            case "assistant" -> MessageRole.assistant;
            case "system" -> MessageRole.system;
            default -> throw new IllegalArgumentException("Unknown MessageRole: " + source);
        };
    }
}
