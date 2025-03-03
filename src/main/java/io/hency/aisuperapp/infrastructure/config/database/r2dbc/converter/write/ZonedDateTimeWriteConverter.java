package io.hency.aisuperapp.infrastructure.config.database.r2dbc.converter.write;

import org.springframework.core.convert.converter.Converter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ZonedDateTimeWriteConverter implements Converter<ZonedDateTime, String> {
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String convert(ZonedDateTime source) {
        return source.format(TIMESTAMP_FORMATTER);
    }
}
