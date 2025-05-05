package io.hency.aisuperapp.features.anthropic.claude.application.port.in;

import io.hency.aisuperapp.features.anthropic.claude.application.vo.ClaudeModel;
import reactor.core.publisher.Flux;

public interface ClaudeMessagesUseCase {
    Flux<?> messages(Object payload, ClaudeModel model);
}
