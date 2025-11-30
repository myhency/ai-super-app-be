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

    @Override
    public Mono<String> sendMessage(String modelName, List<Map<String, String>> messages, Integer maxTokens) {
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
    public Flux<String> sendMessageStream(String modelName, List<Map<String, String>> messages, Integer maxTokens) {
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
}
