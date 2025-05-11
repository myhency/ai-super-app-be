package io.hmg.claude.messages.infrastructure.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import io.hmg.claude.messages.infrastructure.config.ClaudeProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class ClaudeMessagesClient {

    @Value("${external.gcp.credentials.location}")
    private Resource credentialResource;
    private final Map<String, WebClient> webClient;

    public ClaudeMessagesClient(
            @Qualifier("ClaudeMessagesClient") Map<String, WebClient> webClient
    ) {
        this.webClient = webClient;
    }

    public Flux<?> sendChat(Object payload,
                            ClaudeProperties.Resource resource
    ) {
        var accessToken = this.getGcpAccessToken();
        var payloadMap = this.getPayloadMap(payload, resource);

        return sendStream(
                resource.getModel(),
                payloadMap,
                resource.getApiVersion(),
                accessToken
        );
    }

    private Flux<?> sendStream(
            String model,
            Map<String, Object> payload,
            String apiVersion,
            String accessToken
    ) {
        var client = webClient.get(model);

        return client.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/" + model + "@" + apiVersion + ":streamRawPredict")
                        .build()
                )
                .header("Content-Type", "application/json; charset=utf-8")
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(error -> {
                                    log.error("4xx error occurs on ClaudeMessagesClient: {}", error);
                                    return Mono.error(
                                            new RuntimeException(error)
                                    );
                                })
                )
                .onStatus(HttpStatusCode::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .flatMap(error -> {
                                    log.error("5xx error occurs on ClaudeMessagesClient: {}", error);
                                    return Mono.error(
                                            new RuntimeException(error)
                                    );
                                })
                )
                .bodyToFlux(Object.class);
    }

    private String getGcpAccessToken() {
        try {
            var credentials = GoogleCredentials
                    .fromStream(credentialResource.getInputStream())
                    .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
            credentials.refreshIfExpired();
            var accessToken = credentials.getAccessToken().getTokenValue();
            log.info("GCP Access Token: {}", accessToken);
            return accessToken;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> getPayloadMap(Object payload, ClaudeProperties.Resource resource) {
        ObjectMapper objectMapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> payloadMap = objectMapper.convertValue(payload, Map.class);
        payloadMap.remove("model");
        payloadMap.put("anthropic_version", resource.getAnthropicVersion());

        return payloadMap;
    }
}
