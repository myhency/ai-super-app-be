package io.hmg.openai.chat.completion.application.port.out;

import io.hmg.openai.chat.completion.application.domain.vo.ChatCompletionModel;
import reactor.core.publisher.Flux;

public interface ChatCompletionPort {
    Flux<?> sendChat(Object payload, ChatCompletionModel model);
}
