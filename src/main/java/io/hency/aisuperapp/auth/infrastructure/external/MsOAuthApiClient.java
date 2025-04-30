package io.hency.aisuperapp.auth.infrastructure.external;

import io.hency.aisuperapp.auth.adapter.out.dto.TeamsAccessTokenRequest;
import io.hency.aisuperapp.auth.adapter.out.dto.TeamsRefreshTokenRequest;
import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.InternalServerErrorException;
import io.hency.aisuperapp.common.error.exception.UnauthorizedException;
import io.hency.aisuperapp.auth.infrastructure.external.dto.MsOAuthApiClientResponse;
import io.hency.aisuperapp.auth.infrastructure.config.TeamsAuthConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class MsOAuthApiClient {
    private final TeamsAuthConfig teamsAuthConfig;
    private WebClient webClient;
    private final static String uri = "/organizations/oauth2/v2.0/token";

    @PostConstruct
    public void init() {
        webClient = WebClient.builder()
                .baseUrl(teamsAuthConfig.getBaseAuthUrl())
                .build();
    }

    public Mono<MsOAuthApiClientResponse.GetAccessTokenResponse> getAccessToken(TeamsAccessTokenRequest request) {
        return webClient.post()
                .uri(uri)
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
                .body(BodyInserters.fromFormData("client_id", request.clientId())
                        .with("client_secret", teamsAuthConfig.getClientSecret())
                        .with("scope", request.scope())
                        .with("code", request.code())
                        .with("redirect_uri", request.redirectUri())
                        .with("grant_type", request.grantType())
                )
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .doOnNext(log::error)
                        .then(Mono.error(new UnauthorizedException(ErrorCode.H401B)))
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .doOnNext(log::error)
                        .then(Mono.error(new InternalServerErrorException(ErrorCode.H500B)))
                )
                .bodyToMono(MsOAuthApiClientResponse.GetAccessTokenResponse.class);
    }

    public Mono<MsOAuthApiClientResponse.GetAccessTokenResponse> refreshToken(TeamsRefreshTokenRequest request) {
        return webClient.post()
                .uri(uri)
                .headers(httpHeaders -> httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED))
                .body(BodyInserters.fromFormData("client_id", request.clientId())
                        .with("scope", request.scope())
                        .with("refresh_token", request.refreshToken())
                        .with("client_secret", teamsAuthConfig.getClientSecret())
                        .with("grant_type", request.grantType())
                        .with("redirect_uri", request.redirectUri())
                )
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .doOnNext(log::error)
                        .then(Mono.error(new UnauthorizedException(ErrorCode.H401B)))
                )
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .doOnNext(log::error)
                        .then(Mono.error(new InternalServerErrorException(ErrorCode.H500B)))
                )
                .bodyToMono(MsOAuthApiClientResponse.GetAccessTokenResponse.class);

    }
}
