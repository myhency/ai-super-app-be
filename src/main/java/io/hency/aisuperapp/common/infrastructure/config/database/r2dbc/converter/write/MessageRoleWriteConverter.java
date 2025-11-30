package io.hency.aisuperapp.common.infrastructure.config.database.r2dbc.converter.write;

import io.hency.aisuperapp.features.chat.application.domain.vo.MessageRole;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class MessageRoleWriteConverter implements Converter<MessageRole, String> {
    @Override
    public String convert(MessageRole source) {
        return source.getValue();
    }
}
