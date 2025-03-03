package io.hency.aisuperapp.auth.application.port.out;

import io.hency.aisuperapp.auth.constant.AccessType;
import io.hency.aisuperapp.auth.domain.entity.Token;
import io.hency.aisuperapp.infrastructure.client.dto.MsOAuthApiClientResponse;
import reactor.core.publisher.Mono;

public interface TeamsAuthPort {
    Mono<String> saveVerificationCode(String code);
    Mono<String> findVerificationCode(String code);
    Mono<MsOAuthApiClientResponse.GetAccessTokenResponse> getAccessToken(String code);
    Mono<MsOAuthApiClientResponse.GetAccessTokenResponse> refreshToken(String refreshToken);
    Mono<Token> saveToken(Token token);
    Mono<Token> saveAccessType(Token token, AccessType accessType);
    Mono<String> findAccessType(String accessToken);
    Mono<Token> findToken(String accessToken);
}
