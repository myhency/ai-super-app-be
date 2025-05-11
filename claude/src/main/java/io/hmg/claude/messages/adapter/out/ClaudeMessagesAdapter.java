package io.hmg.claude.messages.adapter.out;

import io.hmg.claude.messages.application.port.out.ClaudeMessagesPort;
import io.hmg.claude.messages.application.vo.ClaudeModel;
import io.hmg.claude.messages.infrastructure.config.ClaudeProperties;
import io.hmg.claude.messages.infrastructure.external.ClaudeMessagesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaudeMessagesAdapter implements ClaudeMessagesPort {

    private final ClaudeMessagesClient client;
    private final ClaudeProperties properties;

    @Override
    public Flux<?> sendChat(Object payload, ClaudeModel model) {

        var resource = properties.getResources()
                .stream()
                .filter(res -> res.getModel().equals(model.getName()))
                .findFirst()
                .orElseThrow();

        return client.sendChat(payload, resource)
                .onErrorResume(Flux::error);
    }
}
