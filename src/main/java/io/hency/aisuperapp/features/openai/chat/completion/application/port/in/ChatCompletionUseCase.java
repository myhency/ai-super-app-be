package io.hency.aisuperapp.features.openai.chat.completion.application.port.in;

import io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo.ChatCompletionModel;
import io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo.ChatCompletionPayload;
import reactor.core.publisher.Flux;

public interface ChatCompletionUseCase {
    Flux<?> chatCompletion(ChatCompletionPayload payload, ChatCompletionModel model);
}
