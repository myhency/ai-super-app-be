package io.hency.aisuperapp.features.chat.adapter.out;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hency.aisuperapp.features.chat.application.port.out.LlmPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaudeLlmAdapter implements LlmPort {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    private static final String CLAUDE_API_URL = "http://localhost:8082/v1/messages";
    private static final int MAX_TOOL_ITERATIONS = 5; // tool_use 순환 최대 횟수

    @Override
    public Mono<String> sendMessage(String modelName, List<Map<String, Object>> messages, Integer maxTokens) {
        log.info("sendMessage called with model: {}, messages count: {}", modelName, messages.size());
        Map<String, Object> request = Map.of(
                "model", modelName,
                "messages", messages,
                "max_tokens", maxTokens,
                "stream", false
        );
        log.info("Request created, calling WebClient...");

        return webClientBuilder.build()
                .post()
                .uri(CLAUDE_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(responseMap -> {
                    log.info("Parsed response with keys: {}", responseMap.keySet());
                    try {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> content = (List<Map<String, Object>>) responseMap.get("content");
                        if (content != null && !content.isEmpty()) {
                            String text = (String) content.get(0).get("text");
                            log.info("Extracted text: {}", text);
                            return text;
                        }
                        log.warn("No content in response");
                        return "";
                    } catch (Exception e) {
                        log.error("Failed to extract text from response: {}", e.getMessage(), e);
                        return "Error: Failed to extract text";
                    }
                })
                .doOnSuccess(response -> log.info("Successfully processed Claude response"))
                .doOnError(error -> log.error("Error calling Claude API: {}", error.getMessage()));
    }

    @Override
    public Flux<String> sendMessageStream(String modelName, List<Map<String, Object>> messages, Integer maxTokens) {
        Map<String, Object> request = Map.of(
                "model", modelName,
                "messages", messages,
                "max_tokens", maxTokens,
                "stream", true
        );

        return webClientBuilder.build()
                .post()
                .uri(CLAUDE_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> log.debug("Raw chunk: {}", chunk))
                .map(chunk -> {
                    try {
                        // Parse JSON directly (not SSE format)
                        @SuppressWarnings("unchecked")
                        Map<String, Object> event = objectMapper.readValue(chunk, Map.class);

                        String type = (String) event.get("type");
                        log.debug("Event type: {}", type);
                        if ("content_block_delta".equals(type)) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> delta = (Map<String, Object>) event.get("delta");
                            if (delta != null && "text_delta".equals(delta.get("type"))) {
                                String text = (String) delta.get("text");
                                log.debug("Extracted text: {}", text);
                                return text;
                            }
                        }
                        return "";
                    } catch (Exception e) {
                        log.warn("Failed to parse JSON chunk: {}", e.getMessage());
                        return "";
                    }
                })
                .filter(text -> !text.isEmpty())
                .doOnNext(text -> log.debug("Emitting text chunk: {}", text))
                .doOnComplete(() -> log.debug("Claude streaming completed"))
                .doOnError(error -> log.error("Error in Claude streaming: {}", error.getMessage()));
    }

    @Override
    public Mono<Map<String, Object>> sendMessageWithTools(String modelName, List<Map<String, Object>> messages, Integer maxTokens, List<Map<String, Object>> tools) {
        log.info("sendMessageWithTools called with model: {}, messages count: {}, tools count: {}", modelName, messages.size(), tools.size());
        log.debug("Tools being sent to Claude: {}", tools);

        Map<String, Object> request = Map.of(
                "model", modelName,
                "messages", messages,
                "max_tokens", maxTokens,
                "tools", tools,
                "stream", false
        );

        return webClientBuilder.build()
                .post()
                .uri(CLAUDE_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnSuccess(response -> log.info("Successfully received Claude response with tools"))
                .doOnError(error -> log.error("Error calling Claude API with tools: {}", error.getMessage()));
    }

    @Override
    public Flux<Map<String, Object>> sendMessageStreamWithTools(String modelName, List<Map<String, Object>> messages, Integer maxTokens, List<Map<String, Object>> tools) {
        log.info("sendMessageStreamWithTools called with model: {}, messages count: {}, tools count: {}", modelName, messages.size(), tools.size());

        Map<String, Object> request = Map.of(
                "model", modelName,
                "messages", messages,
                "max_tokens", maxTokens,
                "tools", tools,
                "stream", true
        );

        return webClientBuilder.build()
                .post()
                .uri(CLAUDE_API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnNext(chunk -> log.debug("Raw chunk: {}", chunk))
                .map(chunk -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> event = objectMapper.readValue(chunk, Map.class);
                        return event;
                    } catch (Exception e) {
                        log.warn("Failed to parse JSON chunk: {}", e.getMessage());
                        return Map.<String, Object>of();
                    }
                })
                .filter(event -> !event.isEmpty())
                .doOnComplete(() -> log.debug("Claude streaming with tools completed"))
                .doOnError(error -> log.error("Error in Claude streaming with tools: {}", error.getMessage()));
    }
}
