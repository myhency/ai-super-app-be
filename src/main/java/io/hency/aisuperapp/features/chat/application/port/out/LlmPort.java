package io.hency.aisuperapp.features.chat.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface LlmPort {

    Mono<String> sendMessage(String modelName, List<Map<String, Object>> messages, Integer maxTokens);

    Flux<String> sendMessageStream(String modelName, List<Map<String, Object>> messages, Integer maxTokens);

    Mono<Map<String, Object>> sendMessageWithTools(String modelName, List<Map<String, Object>> messages, Integer maxTokens, List<Map<String, Object>> tools);

    Flux<Map<String, Object>> sendMessageStreamWithTools(String modelName, List<Map<String, Object>> messages, Integer maxTokens, List<Map<String, Object>> tools);
}
