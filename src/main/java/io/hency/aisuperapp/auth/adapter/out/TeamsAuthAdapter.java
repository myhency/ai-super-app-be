package io.hency.aisuperapp.auth.adapter.out;

import io.hency.aisuperapp.auth.adapter.out.dto.TeamsAccessTokenRequest;
import io.hency.aisuperapp.auth.adapter.out.dto.TeamsRefreshTokenRequest;
import io.hency.aisuperapp.auth.application.port.out.TeamsAuthPort;
import io.hency.aisuperapp.auth.constant.AccessType;
import io.hency.aisuperapp.auth.domain.entity.Token;
import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.UnauthorizedException;
import io.hency.aisuperapp.infrastructure.client.MsOAuthApiClient;
import io.hency.aisuperapp.infrastructure.client.dto.MsOAuthApiClientResponse;
import io.hency.aisuperapp.infrastructure.config.auth.TeamsAuthConfig;
import io.hency.aisuperapp.infrastructure.repository.cache.StringCacheRepository;
import io.hency.aisuperapp.infrastructure.repository.cache.TokenCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class TeamsAuthAdapter implements TeamsAuthPort {
    private final static String VERIFICATION_CODE_KEY = "hmgOcean::login::";
    private final static String TOKEN_KEY = "hmgOcean::token::";
    private final static String ACCESS_TYPE_KEY = "hmgOcean::accessType::";

    private final StringCacheRepository stringCacheRepository;
    private final TeamsAuthConfig teamsAuthConfig;
    private final MsOAuthApiClient msOAuthApiClient;
    private final TokenCacheRepository tokenCacheRepository;

    @Override
    public Mono<String> saveVerificationCode(String code) {
        String key = VERIFICATION_CODE_KEY + code;
        String value = String.valueOf(true);
        Duration ttl = Duration.ofSeconds(30);

        return stringCacheRepository.save(
                key,
                value,
                ttl
        );
    }

    @Override
    public Mono<String> findVerificationCode(String code) {
        return stringCacheRepository.findByKey(VERIFICATION_CODE_KEY + code);
    }

    @Override
    public Mono<MsOAuthApiClientResponse.GetAccessTokenResponse> getAccessToken(String code) {
        String scope = "https://graph.microsoft.com/user.read offline_access";
        String grantType = "authorization_code";
        String redirectUri = teamsAuthConfig.getServerUrl() + teamsAuthConfig.getRedirectSegment();

        var request = new TeamsAccessTokenRequest(
                teamsAuthConfig.getClientId(),
                scope,
                code,
                redirectUri,
                grantType
        );

        return msOAuthApiClient.getAccessToken(request);
    }

    @Override
    public Mono<MsOAuthApiClientResponse.GetAccessTokenResponse> refreshToken(String refreshToken) {
        String scope = "https://graph.microsoft.com/user.read offline_access";
        String grantType = "refresh_token";
        String redirectUri = teamsAuthConfig.getServerUrl() + teamsAuthConfig.getRedirectSegment();

        var request = new TeamsRefreshTokenRequest(
                teamsAuthConfig.getClientId(),
                scope,
                redirectUri,
                refreshToken,
                grantType
        );

        return msOAuthApiClient.refreshToken(request);
    }

    @Override
    public Mono<Token> saveToken(Token token) {
        return tokenCacheRepository.save(
                TOKEN_KEY + token.accessToken(),
                token,
                Duration.ofDays(1)
        );
    }

    @Override
    public Mono<Token> saveAccessType(Token token, AccessType accessType) {
        return stringCacheRepository.save(
                        ACCESS_TYPE_KEY + token.accessToken(),
                        accessType.name(),
                        Duration.ofDays(1)
                )
                .thenReturn(token);
    }

    @Override
    public Mono<String> findAccessType(String accessToken) {
        return stringCacheRepository.findByKey(ACCESS_TYPE_KEY + accessToken);
    }

    @Override
    public Mono<Token> findToken(String accessToken) {
        return tokenCacheRepository.findByKey(TOKEN_KEY + accessToken)
                .switchIfEmpty(Mono.error(new UnauthorizedException(ErrorCode.H401H)));
    }


}
