package io.hency.aisuperapp.infrastructure.config.database.r2dbc.converter.write;

import com.github.f4b6a3.ulid.Ulid;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class UlidWriteConverter implements Converter<Ulid, String> {


    @Override
    public String convert(Ulid source) {
        return source.toString();
    }
}
