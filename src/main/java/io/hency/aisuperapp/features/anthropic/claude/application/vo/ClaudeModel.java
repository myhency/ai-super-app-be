package io.hency.aisuperapp.features.anthropic.claude.application.vo;

import io.hency.aisuperapp.common.domain.vo.NameValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaudeModel implements NameValidator.Name {
    CLAUDE_3_5_SONNET_V2("claude-3-5-sonnet-v2"),
    CLAUDE_3_7_SONNET("claude-3-7-sonnet");

    private final String name;

    public static ClaudeModel fromName(String name) {
        return NameValidator.fromName(ClaudeModel.class, name);
    }
}
