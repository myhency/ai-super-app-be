package io.hmg.claude.messages.application.port.in;

import io.hmg.claude.messages.application.vo.ClaudeModel;
import reactor.core.publisher.Flux;

public interface ClaudeMessagesUseCase {
    Flux<?> messages(Object payload, ClaudeModel model);
}
