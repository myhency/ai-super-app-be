package io.hency.aisuperapp.common.infrastructure.config.database.r2dbc.converter.read;

import com.github.f4b6a3.ulid.Ulid;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class UlidReadConverter implements Converter<String, Ulid> {
    @Override
    public Ulid convert(String source) {
        return Ulid.from(source);
    }
}
