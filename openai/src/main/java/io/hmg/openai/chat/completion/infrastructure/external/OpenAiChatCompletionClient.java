package io.hmg.openai.chat.completion.infrastructure.external;

import com.azure.ai.openai.implementation.accesshelpers.ChatCompletionsOptionsAccessHelper;
import com.azure.ai.openai.models.*;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiChatCompletionClient {

    private final Map<String, WebClient> webClients;

    public OpenAiChatCompletionClient(
            @Qualifier("OpenAiChatCompletionClient") Map<String, WebClient> webClients
    ) {
        this.webClients = webClients;
    }

    public Flux<?> sendChat(
            Object payload,
            OpenaiProperties.Resources resource
    ) {
        return send(
                resource.getModel(),
                resource.getDeploymentId(),
                resource.getApiKey(),
                resource.getApiVersion(),
                payload
        );
    }

    private Flux<?> send(
            String model,
            String deploymentId,
            String apiKey,
            String apiVersion,
            Object payload
    ) {
        var client = webClients.get(model);

        var options = getChatCompletionsOptionString();

        return client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/openai/deployments/" + deploymentId + "/chat/completions")
                        .queryParam("api-version", apiVersion)
                        .build()
                )
                .header("api-key", apiKey)
                .bodyValue(options.getBytes(StandardCharsets.UTF_8))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(error -> {
                                    log.error("4xx error occurred while sending chat completion response: {}", error);
                                    return Mono.error(new RuntimeException(error));
                                })
                )
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(error -> {
                                    log.error("5xx error occurred while sending chat completion response: {}", error);
                                    return Mono.error(new RuntimeException(error));
                                })
                )
                .bodyToFlux(String.class)
                .filter(response -> !response.equals("[DONE]"))
                .flatMap(response -> {
                    try {
                        Object res = new ObjectMapper().readValue(
                                response, Object.class
                        );
                        return Flux.just(res);
                    } catch (Exception e) {
                        return Flux.empty();
                    }
                });


    }

    private String getChatCompletionsOptionString() {
        ChatRequestMessage message = new ChatRequestUserMessage("안녕 ");
        ChatCompletionsOptions options = new ChatCompletionsOptions(List.of(message));

        ChatCompletionStreamOptions streamOptions = new ChatCompletionStreamOptions()
                .setIncludeUsage(true);

        ChatCompletionsOptionsAccessHelper.setStream(options, true);
        ChatCompletionsOptionsAccessHelper.setStreamOptions(options, streamOptions);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonWriter jsonWriter = JsonProviders.createWriter(outputStream);
            options.toJson(jsonWriter);
            jsonWriter.close();

            String jsonString = outputStream.toString(StandardCharsets.UTF_8);

            log.info("ChatCompletionsOptions JSON: {}", jsonString);
//            var objectMapper = new ObjectMapper();
//            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
//            };
//            return objectMapper.readValue(jsonString, typeRef);
            return jsonString;
        } catch (IOException e) {
            log.error("Failed to serialize ChatCompletionsOptions to JSON", e);
            throw new RuntimeException(e);
        }
    }
}
