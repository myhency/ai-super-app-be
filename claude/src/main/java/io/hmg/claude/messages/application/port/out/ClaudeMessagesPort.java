package io.hmg.claude.messages.application.port.out;

import io.hmg.claude.messages.application.vo.ClaudeModel;
import reactor.core.publisher.Flux;

public interface ClaudeMessagesPort {
    Flux<?> sendChat(Object payload, ClaudeModel model);
}
