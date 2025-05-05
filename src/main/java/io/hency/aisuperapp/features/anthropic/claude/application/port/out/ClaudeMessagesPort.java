package io.hency.aisuperapp.features.anthropic.claude.application.port.out;

import io.hency.aisuperapp.features.anthropic.claude.application.vo.ClaudeModel;
import reactor.core.publisher.Flux;

public interface ClaudeMessagesPort {
    Flux<?> sendChat(Object payload, ClaudeModel model);
}
