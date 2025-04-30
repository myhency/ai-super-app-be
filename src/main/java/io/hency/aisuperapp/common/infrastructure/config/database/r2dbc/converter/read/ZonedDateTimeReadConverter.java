package io.hency.aisuperapp.common.infrastructure.config.database.r2dbc.converter.read;

import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

import static io.hency.aisuperapp.AiSuperAppApplication.UTC_ZONE;

public class ZonedDateTimeReadConverter implements Converter<LocalDateTime, ZonedDateTime> {
    @Override
    public ZonedDateTime convert(@NonNull LocalDateTime source) {
        return ZonedDateTime.of(source, UTC_ZONE);
    }
}
