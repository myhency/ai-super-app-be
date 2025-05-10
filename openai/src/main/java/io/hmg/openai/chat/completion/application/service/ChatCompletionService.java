package io.hmg.openai.chat.completion.application.service;

import io.hmg.openai.chat.completion.application.domain.vo.ChatCompletionModel;
import io.hmg.openai.chat.completion.application.port.in.ChatCompletionUseCase;
import io.hmg.openai.chat.completion.application.port.out.ChatCompletionPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatCompletionService implements ChatCompletionUseCase {

    private final ChatCompletionPort chatCompletionPort;

    @Override
    public Flux<?> chatCompletion(Object payload, ChatCompletionModel model) {
        Flux<?> result = chatCompletionPort.sendChat(payload, model)
                .cache();

        return result.collectList()
                .doOnNext(list -> {
                    log.info("Send chat completion result: {}", list);
                })
                .thenMany(result);
    }
}
