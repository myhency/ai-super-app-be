package io.hency.aisuperapp.features.chat.application.domain.vo;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageRole {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    private final String value;

    MessageRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
