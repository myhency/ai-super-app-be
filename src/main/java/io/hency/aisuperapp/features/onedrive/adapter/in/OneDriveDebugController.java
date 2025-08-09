package io.hency.aisuperapp.features.onedrive.adapter.in;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class OneDriveDebugController {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://graph.microsoft.com/v1.0")
            .build();

    @GetMapping("/me")
    public Mono<String> getMe(@RequestHeader("Authorization") String authorization) {
        String accessToken = extractToken(authorization);
        return webClient.get()
                .uri("/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class);
    }

    @GetMapping("/drive")
    public Mono<String> getDrive(@RequestHeader("Authorization") String authorization) {
        String accessToken = extractToken(authorization);
        return webClient.get()
                .uri("/me/drive")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .doOnNext(body -> System.out.println("Drive error: " + body))
                        .then(Mono.error(new RuntimeException("Drive API error: " + response.statusCode())))
                )
                .bodyToMono(String.class);
    }

    @GetMapping("/drives")
    public Mono<String> getDrives(@RequestHeader("Authorization") String authorization) {
        String accessToken = extractToken(authorization);
        return webClient.get()
                .uri("/me/drives")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class);
    }

    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        throw new IllegalArgumentException("Invalid authorization header");
    }
}