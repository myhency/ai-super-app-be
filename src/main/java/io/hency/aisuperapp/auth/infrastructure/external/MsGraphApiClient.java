package io.hency.aisuperapp.auth.infrastructure.external;

import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.InternalServerErrorException;
import io.hency.aisuperapp.common.error.exception.TokenExpiredException;
import io.hency.aisuperapp.auth.infrastructure.external.dto.MsGraphApiClientResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MsGraphApiClient {
    private final static String baseUrl = "https://graph.microsoft.com";
    private WebClient webClient;

    @PostConstruct
    public void init() {
        webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_JSON))
                .build();
    }

    public Mono<MsGraphApiClientResponse.GetTeamsUserResponse> getTeamsUser(String accessToken) {
        return webClient.get()
                .uri("/v1.0/me")
                .headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new TokenExpiredException(ErrorCode.H401E)))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new InternalServerErrorException(ErrorCode.H500D))))
                .bodyToMono(MsGraphApiClientResponse.GetTeamsUserResponse.class);
    }
}
