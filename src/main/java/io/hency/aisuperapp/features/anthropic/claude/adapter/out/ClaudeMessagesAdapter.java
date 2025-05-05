package io.hency.aisuperapp.features.anthropic.claude.adapter.out;

import io.hency.aisuperapp.features.anthropic.claude.application.port.out.ClaudeMessagesPort;
import io.hency.aisuperapp.features.anthropic.claude.application.vo.ClaudeModel;
import io.hency.aisuperapp.features.anthropic.claude.infrastructure.config.ClaudeProperties;
import io.hency.aisuperapp.features.anthropic.claude.infrastructure.external.ClaudeMessagesClient;
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
