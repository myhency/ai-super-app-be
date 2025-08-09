package io.hmg.claude.messages.application.vo;

import io.hmg.claude.common.domain.vo.NameValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClaudeModel implements NameValidator.Name {
    CLAUDE_OPUS_4("claude-opus-4"),
    CLAUDE_3_5_SONNET_V2("claude-3-5-sonnet-v2"),
    CLAUDE_CODE_SONNET_4("claude-sonnet-4-20250514"),
    CLAUDE_CODE_HAIKU_3_5("claude-3-5-haiku-20241022"),
    CLAUDE_3_7_SONNET("claude-3-7-sonnet");

    private final String name;

    public static ClaudeModel fromName(String name) {
        return NameValidator.fromName(ClaudeModel.class, name);
    }
}
