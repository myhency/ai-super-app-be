package io.hmg.openai.chat.completion.application.service;

import io.hmg.openai.chat.completion.application.domain.vo.ChatCompletionModel;
import io.hmg.openai.chat.completion.application.port.in.ChatCompletionUseCase;
import io.hmg.openai.chat.completion.application.port.out.ChatCompletionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatCompletionService implements ChatCompletionUseCase {

    private final ChatCompletionPort chatCompletionPort;

    @Override
    public Flux<?> chatCompletion(Object payload, ChatCompletionModel model) {
        List<Object> items = new ArrayList<>();

        return chatCompletionPort.sendChat(payload, model)
                .doOnNext(item -> {
                    items.add(item);
                    log.debug("Received item: {}", item);
                })
                .doOnComplete(() -> {
                    log.info("Send chat completion result: {}", items);
                })
                .doOnError(error -> {
                    log.error("Chat completion error: {}", error.getMessage());
                });
    }
}
