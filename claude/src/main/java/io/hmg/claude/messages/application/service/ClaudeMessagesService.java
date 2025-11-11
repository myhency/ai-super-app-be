package io.hmg.claude.messages.application.service;

import io.hmg.claude.messages.application.port.in.ClaudeMessagesUseCase;
import io.hmg.claude.messages.application.port.out.ClaudeMessagesPort;
import io.hmg.claude.messages.application.vo.ClaudeModel;
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
//                            .subscribe(
//                                    fullResponseList -> log.info("Full chat result for claude messages: {}", fullResponseList)
//                            );
                            .subscribe();
                });
    }
}
