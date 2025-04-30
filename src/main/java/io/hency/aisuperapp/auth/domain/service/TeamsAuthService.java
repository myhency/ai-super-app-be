package io.hency.aisuperapp.auth.domain.service;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.auth.application.port.in.TeamsAuthUseCase;
import io.hency.aisuperapp.auth.application.port.out.TeamsAuthPort;
import io.hency.aisuperapp.auth.constant.AccessType;
import io.hency.aisuperapp.auth.domain.entity.Token;
import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.UnauthorizedException;
import io.hency.aisuperapp.common.util.JwtUtils;
import io.hency.aisuperapp.features.user.application.port.in.UserUseCase;
import io.hency.aisuperapp.features.user.application.port.out.UserPort;
import io.hency.aisuperapp.features.user.application.domain.entity.User;
import io.hency.aisuperapp.features.userlimit.application.port.out.UserLimitPort;
import io.hency.aisuperapp.infrastructure.config.auth.TeamsAuthConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class TeamsAuthService implements TeamsAuthUseCase {
    private final UserPort userPort;
    private final TeamsAuthPort teamsAuthPort;
    private final UserUseCase userUseCase;
    private final UserLimitPort userLimitPort;
    private final TeamsAuthConfig teamsAuthConfig;

    @Override
    public Mono<User> authorize(String accessToken, String ipAddress) {
        return userPort.findUserFromCacheBy(accessToken)
                .switchIfEmpty(this.createNewTeamsUser(accessToken));
    }

    @Override
    public Mono<Token> refreshToken(String expiredToken) {
        Mono<Token> expiredTokenMono = teamsAuthPort.findToken(expiredToken)
                .cache();

        return expiredTokenMono
                .flatMap(token -> teamsAuthPort.refreshToken(token.refreshToken()))
                .flatMap(newToken -> {
                    var tokenValidateMono = Mono.just(JwtUtils.parseClaims(newToken.getIdToken()))
                            .filter(claims -> teamsAuthConfig.getClientId().equals(claims.getAudience()))
                            .filter(claims -> teamsAuthConfig.getTenantIds().contains(String.valueOf(claims.get("tid"))))
                            .switchIfEmpty(Mono.error(new UnauthorizedException(ErrorCode.H401D)))
                            .then();

                    return Mono.when(tokenValidateMono)
                            .thenReturn(newToken);
                })
                .zipWith(expiredTokenMono)
                .flatMap(tuple -> {
                    var refreshedToken = tuple.getT1();
                    var expToken = tuple.getT2();
                    Token token = refreshedToken.toToken(expToken.applicationType());

                    return teamsAuthPort.saveToken(token)
                            .subscribeOn(Schedulers.boundedElastic())
                            .thenReturn(token);
                })
                .switchIfEmpty(Mono.error(new UnauthorizedException(ErrorCode.H401I)));
    }

    private Mono<User> createNewTeamsUser(String accessToken) {
        return userPort.getTeamsUser(accessToken)
                .flatMap(teamsUser -> userUseCase.createUser(accessToken, teamsUser))
                .flatMap(user -> this.upsertUserAccessType(user, accessToken))
                .flatMap(this::updateUserLastAccessedAt)
                .flatMap(user -> this.saveUserToCache(accessToken, user))
                .doOnError(ex -> log.error("Failed to create a new user", ex));
    }

    private Mono<User> upsertUserAccessType(User user, String accessToken) {
        return teamsAuthPort.findAccessType(accessToken)
                .flatMap(accessType ->
                        userLimitPort.upsertUserAccessType(user, AccessType.valueOf(accessType))
                )
                .subscribeOn(Schedulers.boundedElastic())
                .thenReturn(user);
    }

    private Mono<User> updateUserLastAccessedAt(User user) {
        return userPort.updateLastAccessedAt(Ulid.from(user.id()))
                .subscribeOn(Schedulers.boundedElastic())
                .thenReturn(user);
    }

    private Mono<User> saveUserToCache(String accessToken, User user) {
        return userPort.saveUserToCache(accessToken, user)
                .subscribeOn(Schedulers.boundedElastic())
                .thenReturn(user);
    }
}
