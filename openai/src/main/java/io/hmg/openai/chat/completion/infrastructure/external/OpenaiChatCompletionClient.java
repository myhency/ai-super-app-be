package io.hmg.openai.chat.completion.infrastructure.external;

import com.azure.ai.openai.models.*;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hmg.openai.chat.completion.infrastructure.config.OpenaiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class OpenaiChatCompletionClient {

    private final Map<String, WebClient> webClients;

    public OpenaiChatCompletionClient(@Qualifier("OpenAiChatCompletionClient") Map<String, WebClient> webClients) {
        this.webClients = webClients;
    }

    public Flux<?> sendChat(Object payload,
                            OpenaiProperties.Resources resource
    ) {
        return send(resource.getModel(),
                resource.getDeploymentId(),
                resource.getApiKey(),
                resource.getApiVersion(),
                payload
        );
    }

    private Flux<?> send(String model,
                         String deploymentId,
                         String apiKey,
                         String apiVersion,
                         Object payload
    ) {
        var client = webClients.get(model);
        var options = getChatCompletionsOptionString(payload);

        return client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/openai/deployments/" + deploymentId + "/chat/completions")
                        .queryParam("api-version", apiVersion)
                        .build())
                .header("api-key", apiKey)
                .bodyValue(options.getBytes(StandardCharsets.UTF_8))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(error -> {
                            log.error("4xx error occurred while sending chat completion response: {}", error);
                            return Mono.error(new RuntimeException(error));
                        }))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(error -> {
                            log.error("5xx error occurred while sending chat completion response: {}", error);
                            return Mono.error(new RuntimeException(error));
                        }))
                .bodyToFlux(String.class)
                .filter(response -> !response.equals("[DONE]"))
                .flatMap(response -> {
                    try {
                        Object res = new ObjectMapper().readValue(response, Object.class);
                        return Flux.just(res);
                    } catch (Exception e) {
                        return Flux.empty();
                    }
                });
    }

    private String getChatCompletionsOptionString(Object payload) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (payload instanceof ChatCompletionsOptions options) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                JsonWriter jsonWriter = JsonProviders.createWriter(outputStream);
                options.toJson(jsonWriter);
                jsonWriter.close();
                return outputStream.toString(StandardCharsets.UTF_8);
            } else if (payload instanceof Map) {
                String jsonString = objectMapper.writeValueAsString(payload);
                log.debug("Converted Map to JSON: {}", jsonString);
                return jsonString;
            } else {
                String jsonString = objectMapper.writeValueAsString(payload);
                log.debug("Serialized Object to JSON: {}", jsonString);
                return jsonString;
            }
        } catch (IOException e) {
            log.error("Failed to serialize payload to JSON", e);
            throw new RuntimeException("Failed to serialize payload to JSON: " + e.getMessage(), e);
        }
    }
}
