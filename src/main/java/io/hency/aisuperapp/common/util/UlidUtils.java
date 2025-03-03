package io.hency.aisuperapp.common.util;

import com.github.f4b6a3.ulid.Ulid;
import org.springframework.util.StringUtils;

public class UlidUtils {
    public static Ulid of(String input) {
        return StringUtils.hasText(input) ? Ulid.from(input) : null;
    }
}
