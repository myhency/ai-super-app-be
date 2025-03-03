package io.hency.aisuperapp.features.user.domain.service;

import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.UnauthorizedException;
import io.hency.aisuperapp.common.util.JwtUtils;
import io.hency.aisuperapp.features.user.adapter.out.dto.TeamsUser;
import io.hency.aisuperapp.features.user.application.port.in.UserUseCase;
import io.hency.aisuperapp.features.user.application.port.out.UserPort;
import io.hency.aisuperapp.features.user.domain.entity.User;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {
    private final UserPort userPort;

    @Override
    public Mono<User> createUser(String accessToken, TeamsUser teamsUser) {
        return userPort.findUserByUserKey(teamsUser.id())
                .switchIfEmpty(createTeamsUser(accessToken, teamsUser))
                .cache();
    }

    private Mono<User> createTeamsUser(String accessToken, TeamsUser teamsUser) {
        return Mono.defer(() -> {
            Claims claims = JwtUtils.parseClaims(accessToken);
            String tenantId = claims.get("tid") != null ? String.valueOf(claims.get("tid")) : null;
            if (tenantId == null) {
                log.error("Tenant ID is missing in JWT claims");
                return Mono.error(new UnauthorizedException(ErrorCode.H401F));
            }
            String subscribeId = "a26d67ed-67ae-4b3b-af2c-6a4d3838cc05";
            return userPort.create(tenantId, teamsUser.id(), teamsUser.displayName(), teamsUser.userPrincipalName(), subscribeId);
        });
    }
}
