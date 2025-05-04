package io.hency.aisuperapp.features.openai.chat.completion.application.port.out;

import io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo.ChatCompletionModel;
import io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo.ChatCompletionPayload;
import reactor.core.publisher.Flux;

public interface ChatCompletionPort {
    Flux<?> sendChat(Object payload, ChatCompletionModel model);
}
