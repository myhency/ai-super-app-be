package io.hmg.claude.messages.infrastructure.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hmg.claude.messages.infrastructure.config.AwsBedrockProperties;
import io.hmg.claude.messages.infrastructure.service.AwsSignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BedrockMessagesClient {

    private final AwsSignatureService awsSignatureService;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Flux<?> sendChat(Object payload, AwsBedrockProperties.Model model) {
        return Mono.fromCallable(() -> {
            PayloadInfo payloadInfo = preparePayload(payload);
            return payloadInfo;
        }).flatMapMany(payloadInfo -> {
            boolean isStreaming = payloadInfo.isStreaming();
            String jsonBody = payloadInfo.getJsonBody();

            // Use different endpoint for streaming
            String uri = isStreaming
                    ? "/model/" + model.getModelId() + "/invoke-with-response-stream"
                    : "/model/" + model.getModelId() + "/invoke";
            String region = model.getRegion();

            return awsSignatureService.signRequest(uri, jsonBody, region)
                    .flatMapMany(headers -> {
                        return webClientBuilder
                                .baseUrl("https://bedrock-runtime." + region + ".amazonaws.com")
                                .build()
                                .post()
                                .uri(uri)
                                .header("Authorization", headers.get("Authorization"))
                                .header("X-Amz-Date", headers.get("X-Amz-Date"))
                                .header("Content-Type", "application/json")
                                .bodyValue(jsonBody)
                                .retrieve()
                                .onStatus(HttpStatusCode::is4xxClientError,
                                        clientResponse -> clientResponse.bodyToMono(String.class)
                                                .flatMap(error -> {
                                                    log.error("4xx error from Bedrock: {}", error);
                                                    return Mono.error(new RuntimeException(error));
                                                })
                                )
                                .onStatus(HttpStatusCode::is5xxServerError,
                                        clientResponse -> clientResponse.bodyToMono(String.class)
                                                .flatMap(error -> {
                                                    log.error("5xx error from Bedrock: {}", error);
                                                    return Mono.error(new RuntimeException(error));
                                                })
                                )
                                .bodyToFlux(String.class)
                                .flatMap(chunk -> {
                                    if (isStreaming) {
                                        // Parse AWS event stream format
                                        return parseBedrockEventStream(chunk);
                                    } else {
                                        return Mono.just(chunk);
                                    }
                                });
                    });
        });
    }

    private PayloadInfo preparePayload(Object payload) throws JsonProcessingException {
        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, Map.class);

        // Check if streaming is requested
        boolean isStreaming = payloadMap.containsKey("stream") &&
                             Boolean.TRUE.equals(payloadMap.get("stream"));

        // Remove fields that Bedrock doesn't support
        payloadMap.remove("model");
        payloadMap.remove("stream");  // Bedrock doesn't support stream parameter in request body
        payloadMap.remove("container");
        payloadMap.remove("mcp_servers");
        payloadMap.remove("service_tier");

        // Add anthropic_version if not present
        if (!payloadMap.containsKey("anthropic_version")) {
            payloadMap.put("anthropic_version", "bedrock-2023-05-31");
        }

        String jsonBody = objectMapper.writeValueAsString(payloadMap);
        return new PayloadInfo(jsonBody, isStreaming);
    }

    private Flux<String> parseBedrockEventStream(String chunk) {
        try {
            // AWS Bedrock returns event-stream format with structure like:
            // :event-type chunk
            // :content-type application/json
            // :message-type event
            // {"bytes":"base64EncodedJson"}

            // A chunk may contain multiple events, so we need to find all of them
            java.util.List<String> events = new java.util.ArrayList<>();
            int searchStart = 0;

            while (true) {
                int jsonStart = chunk.indexOf("{\"bytes\":\"", searchStart);
                if (jsonStart == -1) {
                    break;
                }

                // Find the closing brace for this JSON object
                int jsonEnd = chunk.indexOf("}", jsonStart);
                if (jsonEnd == -1) {
                    log.warn("Incomplete JSON in chunk at position {}", jsonStart);
                    break;
                }

                String jsonPart = chunk.substring(jsonStart, jsonEnd + 1);

                try {
                    // Parse to extract base64 encoded bytes
                    @SuppressWarnings("unchecked")
                    Map<String, String> bytesMap = objectMapper.readValue(jsonPart, Map.class);
                    String base64Bytes = bytesMap.get("bytes");

                    if (base64Bytes != null && !base64Bytes.isEmpty()) {
                        // Decode base64 to get actual JSON content
                        byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Bytes);
                        String decodedJson = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);

                        // Just return the JSON, don't add "data:" prefix here
                        // The SSE format will be handled by the framework
                        events.add(decodedJson);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse JSON at position {}: {}", jsonStart, e.getMessage());
                }

                // Move search position forward
                searchStart = jsonEnd + 1;
            }

            if (events.isEmpty()) {
                return Flux.empty();
            }

            return Flux.fromIterable(events);

        } catch (Exception e) {
            log.error("Failed to parse Bedrock event stream: {}", e.getMessage(), e);
            return Flux.error(new RuntimeException("Failed to parse event stream", e));
        }
    }

    private static class PayloadInfo {
        private final String jsonBody;
        private final boolean streaming;

        public PayloadInfo(String jsonBody, boolean streaming) {
            this.jsonBody = jsonBody;
            this.streaming = streaming;
        }

        public String getJsonBody() {
            return jsonBody;
        }

        public boolean isStreaming() {
            return streaming;
        }
    }
}