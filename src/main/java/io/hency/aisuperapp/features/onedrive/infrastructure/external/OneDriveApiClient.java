package io.hency.aisuperapp.features.onedrive.infrastructure.external;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class OneDriveApiClient {

    private final WebClient webClient;
    private static final String GRAPH_API_BASE_URL = "https://graph.microsoft.com/v1.0";

    public OneDriveApiClient() {
        this.webClient = WebClient.builder()
                .baseUrl(GRAPH_API_BASE_URL)
                .build();
    }

    public Mono<String> getFiles(String accessToken) {
        return webClient.get()
                .uri("/me/drive")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .doOnNext(body -> System.out.println("Error response: " + body))
                        .then(Mono.error(new RuntimeException("Microsoft Graph API error: " + response.statusCode())))
                )
                .bodyToMono(String.class)
                .flatMap(driveResponse -> {
                    System.out.println("Drive response: " + driveResponse);
                    return webClient.get()
                            .uri("/me/drive/root/children")
                            .header("Authorization", "Bearer " + accessToken)
                            .retrieve()
                            .onStatus(
                                status -> status.is4xxClientError() || status.is5xxServerError(),
                                response -> response.bodyToMono(String.class)
                                    .doOnNext(body -> System.out.println("Files error response: " + body))
                                    .then(Mono.error(new RuntimeException("Microsoft Graph API files error: " + response.statusCode())))
                            )
                            .bodyToMono(String.class);
                });
    }

    public Mono<String> getFileContent(String accessToken, String fileId) {
        return webClient.get()
                .uri("/me/drive/items/{fileId}/content", fileId)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class);
    }

    public Mono<String> uploadFile(String accessToken, String fileName, byte[] content) {
        return webClient.put()
                .uri("/me/drive/root:/{fileName}:/content", fileName)
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/octet-stream")
                .bodyValue(content)
                .retrieve()
                .bodyToMono(String.class);
    }
}