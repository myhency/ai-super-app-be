package io.hency.aisuperapp.infrastructure.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.InternalServerErrorException;
import io.hency.aisuperapp.infrastructure.client.dto.OpenAIApiClientRequest;
import io.hency.aisuperapp.infrastructure.client.dto.OpenAIApiClientResponse;
import io.hency.aisuperapp.infrastructure.config.azure.openai.AzureOpenAIConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class OpenAIApiClient {
    public Flux<OpenAIApiClientResponse> sendMessage(
            List<OpenAIApiClientRequest.ChatRequestMessage> messages,
            AzureOpenAIConfig.ApiResource resource
    ) {
        var deploymentId = resource.getDeploymentId();
        var apiVersion = "2024-08-01-preview";

        var payload = OpenAIApiClientRequest.builder()
                .messages(messages)
                .maxTokens(resource.getMaxToken())
                .stream(true)
                .build();

        WebClient client = azureOpenAIWebClient(resource);

        return client
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/openai/deployments/{deploymentId}/chat/completions")
                        .queryParam("api-version", apiVersion)
                        .build(deploymentId))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> {
                                    log.error("Client error: {}", error);
                                    return Mono.error(new InternalServerErrorException(ErrorCode.H500F));
                                }))
                .bodyToFlux(String.class)
                .filter(data -> !data.equals("[DONE]"))
                .map(data -> {
                    try {
                        return new ObjectMapper().readValue(data, OpenAIApiClientResponse.class);
                    } catch (JsonProcessingException e) {
                        log.error("Error parsing response: {}", data, e);
                        throw new RuntimeException("Error parsing OpenAI response", e);
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error in stream processing: ", e);
                    return Mono.error(new InternalServerErrorException(ErrorCode.H500F));
                });
    }

    private WebClient azureOpenAIWebClient(AzureOpenAIConfig.ApiResource resource) {
        return WebClient.builder()
                .baseUrl(resource.getUrl())
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
                    httpHeaders.set("api-key", resource.getApiKey());
                })
                .codecs(clientCodecConfigurer -> {
                    clientCodecConfigurer.defaultCodecs()
                            .maxInMemorySize(16 * 1024 * 1024);
                })
                .build();
    }
}
