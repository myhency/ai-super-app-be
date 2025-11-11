package io.hmg.claude.common.domain.vo;

public class NameValidator {
    public static <T extends Enum<T> & Name> T fromName(Class<T> enumType, String name) {
        for (T constant : enumType.getEnumConstants()) {
            if (constant.getName().equals(name)) {
                return constant;
            }
        }
        throw new RuntimeException("name is :" + name);
    }

    public interface Name {
        String getName();
    }
}
