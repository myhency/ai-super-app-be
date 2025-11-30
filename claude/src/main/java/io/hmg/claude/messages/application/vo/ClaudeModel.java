package io.hmg.claude.messages.application.vo;

import io.hmg.claude.common.domain.vo.NameValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@RequiredArgsConstructor
public enum ClaudeModel implements NameValidator.Name {
    CLAUDE_OPUS_4("claude-opus-4"),
    CLAUDE_OPUS_4_1("claude-opus-4-1-20250805"),
    CLAUDE_3_5_SONNET_V2("claude-3-5-sonnet-v2"),
    CLAUDE_CODE_SONNET_4("claude-sonnet-4-20250514"),
    CLAUDE_CODE_HAIKU_3_5("claude-3-5-haiku-20241022"),
    CLAUDE_3_7_SONNET_V2("claude-3-7-sonnet-20250219"),
    CLAUDE_3_7_SONNET("claude-3-7-sonnet"),

    // AWS Bedrock models
    BEDROCK_CLAUDE_OPUS_4_5("anthropic.claude-opus-4-5-20251101"),
    BEDROCK_CLAUDE_HAIKU_4_5("anthropic.claude-haiku-4-5-20251001"),
    BEDROCK_CLAUDE_SONNET_4_5("anthropic.claude-sonnet-4-5-20250929");

    private final String name;

    public static ClaudeModel fromName(String name) {
        log.info("name is {}", name);
        // Map Claude Code model names to Bedrock model names
        String mappedName = switch (name) {
            case "claude-haiku-4-5-20251001" -> "anthropic.claude-haiku-4-5-20251001";
            case "claude-sonnet-4-5-20250929" -> "anthropic.claude-sonnet-4-5-20250929";
            default -> name;
        };
        return NameValidator.fromName(ClaudeModel.class, mappedName);
    }
}
