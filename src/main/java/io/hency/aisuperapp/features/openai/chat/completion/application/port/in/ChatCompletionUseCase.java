package io.hency.aisuperapp.features.openai.chat.completion.application.port.in;

import io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo.ChatCompletionModel;
import reactor.core.publisher.Flux;

public interface ChatCompletionUseCase {
    Flux<?> chatCompletion(Object payload, ChatCompletionModel model);
}
