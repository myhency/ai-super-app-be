package io.hency.aisuperapp.features.anthropic.claude.application.service;

import io.hency.aisuperapp.features.anthropic.claude.application.port.in.ClaudeMessagesUseCase;
import io.hency.aisuperapp.features.anthropic.claude.application.port.out.ClaudeMessagesPort;
import io.hency.aisuperapp.features.anthropic.claude.application.vo.ClaudeModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaudeMessagesService implements ClaudeMessagesUseCase {

    private final ClaudeMessagesPort claudeMessagesPort;

    @Override
    public Flux<?> messages(Object payload, ClaudeModel model) {
        Flux<?> result = claudeMessagesPort.sendChat(payload, model).cache();

        return result
                .doOnComplete(() -> {
                    result.collectList()
                            .subscribeOn(Schedulers.boundedElastic())
                            .subscribe(
                                    fullResponseList -> log.info("Full chat result for claude messages: {}", fullResponseList)
                            );
                });
    }
}
