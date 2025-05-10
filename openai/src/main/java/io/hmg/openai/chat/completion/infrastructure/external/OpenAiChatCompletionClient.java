package io.hmg.openai.chat.completion.infrastructure.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hmg.openai.chat.completion.infrastructure.config.OpenaiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

        return client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/openai/deployments/" + deploymentId + "/chat/completions")
                        .queryParam("api-version", apiVersion)
                        .build()
                )
                .header("api-key", apiKey)
                .bodyValue(payload)
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
}
