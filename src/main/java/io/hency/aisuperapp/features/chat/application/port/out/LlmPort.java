package io.hency.aisuperapp.features.chat.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface LlmPort {

    Mono<String> sendMessage(String modelName, List<Map<String, String>> messages, Integer maxTokens);

    Flux<String> sendMessageStream(String modelName, List<Map<String, String>> messages, Integer maxTokens);
}
