package io.hency.aisuperapp.features.openai.chat.completion.adapter.out;

import io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo.ChatCompletionModel;
import io.hency.aisuperapp.features.openai.chat.completion.application.port.out.ChatCompletionPort;
import io.hency.aisuperapp.features.openai.chat.completion.infrastructure.config.OpenaiProperties;
import io.hency.aisuperapp.features.openai.chat.completion.infrastructure.external.OpenAiChatCompletionClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatCompletionAdapter implements ChatCompletionPort {

    private final OpenaiProperties properties;
    private final OpenAiChatCompletionClient client;

    @Override
    public Flux<?> sendChat(Object payload, ChatCompletionModel model) {
        var resource = properties.getResources()
                .stream()
                .filter(resources -> resources.getModel().equals(model.getName()))
                .findFirst()
                .orElseThrow();
        return client.sendChat(payload, resource)
                .onErrorResume(Flux::error);
    }
}
