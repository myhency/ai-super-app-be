package io.hmg.claude.messages.application.service;

import io.hmg.claude.messages.application.port.in.ClaudeMessagesUseCase;
import io.hmg.claude.messages.application.port.out.ClaudeMessagesPort;
import io.hmg.claude.messages.application.vo.ClaudeModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeMessagesService implements ClaudeMessagesUseCase {

    private final ClaudeMessagesPort claudeMessagesPort;

    @Override
    public Flux<?> messages(Object payload, ClaudeModel model) {
        return claudeMessagesPort.sendChat(payload, model);
    }
}
