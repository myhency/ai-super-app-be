package io.hency.aisuperapp.auth.application.service;

import com.github.f4b6a3.ulid.UlidCreator;
import io.hency.aisuperapp.auth.application.port.in.TeamsLoginUseCase;
import io.hency.aisuperapp.auth.application.port.out.TeamsAuthPort;
import io.hency.aisuperapp.auth.constant.AccessType;
import io.hency.aisuperapp.auth.constant.ApplicationType;
import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.UnauthorizedException;
import io.hency.aisuperapp.common.util.JwtUtils;
import io.hency.aisuperapp.auth.infrastructure.external.dto.MsOAuthApiClientResponse;
import io.hency.aisuperapp.auth.infrastructure.config.TeamsAuthConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamsLoginService implements TeamsLoginUseCase {
    private final TeamsAuthConfig teamsAuthConfig;
    private final TeamsAuthPort teamsAuthPort;

    @Override
    public Mono<String> createTeamsLoginUrl() {
        String verificationCode = UlidCreator.getMonotonicUlid().toString();
        String redirectUrl = teamsAuthConfig.getServerUrl() + teamsAuthConfig.getRedirectSegment();
        return teamsAuthConfig.generateAuthorizeUrl(
                        teamsAuthConfig.getBaseAuthUrl(),
                        teamsAuthConfig.getClientId(),
                        verificationCode,
                        redirectUrl
                )
                .flatMap(authorizeUrl -> teamsAuthPort
                        .saveVerificationCode(verificationCode)
                        .subscribeOn(Schedulers.boundedElastic())
                        .thenReturn(authorizeUrl)
                );
    }

    @Override
    public Mono<String> login(String code, String verificationCode) {
        return teamsAuthPort.findVerificationCode(verificationCode)
                .switchIfEmpty(Mono.error(new UnauthorizedException(ErrorCode.H401A)))
                .then(teamsAuthPort.getAccessToken(code))
                .flatMap(getAccessTokenResponse ->
                        this.validateToken(getAccessTokenResponse)
                                .thenReturn(getAccessTokenResponse)
                )
                .flatMap(getAccessTokenResponse ->
                        this.saveToken(getAccessTokenResponse)
                                .thenReturn(getAccessTokenResponse)
                )
                .flatMap(getAccessTokenResponse ->
                        this.saveAccessType(getAccessTokenResponse)
                                .thenReturn(getAccessTokenResponse.getAccessToken())
                );
    }

    private Mono<Void> validateToken(MsOAuthApiClientResponse.GetAccessTokenResponse response) {
        var token = response.getAccessToken();
        return Mono.fromCallable(() -> JwtUtils.parseClaims(token))
                .flatMap(claims -> {
                    if (!teamsAuthConfig.getClientId().equals(claims.get("appid"))) {
                        log.error("Invalid appid: {}", claims.get("appid"));
                        return Mono.error(new UnauthorizedException(ErrorCode.H401D));
                    }
                    if (!teamsAuthConfig.getTenantIds().contains(String.valueOf(claims.get("tid")))) {
                        log.error("Invalid tenant id: {}", claims.get("tid"));
                        return Mono.error(new UnauthorizedException(ErrorCode.H401C));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> saveToken(MsOAuthApiClientResponse.GetAccessTokenResponse response) {
        var token = response.toToken(ApplicationType.BROWSER);
        return teamsAuthPort.saveToken(token)
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private Mono<Void> saveAccessType(MsOAuthApiClientResponse.GetAccessTokenResponse response) {
        var token = response.toToken(ApplicationType.BROWSER);
        return teamsAuthPort.saveAccessType(token, AccessType.TEAMS_BROWSER)
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
