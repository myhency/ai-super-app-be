package io.hency.aisuperapp.common.infrastructure.config.web.filter;

import io.hency.aisuperapp.auth.application.port.in.TeamsAuthUseCase;
import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.TokenExpiredException;
import io.hency.aisuperapp.common.error.exception.UnauthorizedException;
import io.hency.aisuperapp.common.util.HttpUtils;
import io.hency.aisuperapp.features.user.application.domain.entity.User;
import io.hency.aisuperapp.common.infrastructure.config.web.context.RequestContextHolder;
import io.hency.aisuperapp.common.infrastructure.config.web.context.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Order(99)
public class AuthWebFilter extends BaseAuthWebFilter {
    private final TeamsAuthUseCase teamsAuthUseCase;

    public AuthWebFilter(PathMatcher pathMatcher, TeamsAuthUseCase teamsAuthUseCase) {
        super(pathMatcher);
        this.teamsAuthUseCase = teamsAuthUseCase;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();

        if (isOptionsMethod(exchange) || isExcludeUrl(exchange)) {
            return chain.filter(exchange);
        }

        final String accessToken = Optional.of(HttpUtils.extractBearerToken(request.getHeaders()))
                .filter(token -> !token.isEmpty())
                .orElseThrow(() -> {
                    log.warn("Access token is not found in the request header");
                    return new UnauthorizedException(ErrorCode.H401G);
                });
        final String ipAddress = Optional.ofNullable(request.getHeaders().getFirst("X-Real-Ip"))
                .orElse(request.getRemoteAddress().getAddress().getHostAddress());

        return teamsAuthUseCase.authorize(accessToken, ipAddress)
                .onErrorResume(ex -> {
                    if (ex instanceof TokenExpiredException) {
                        return refreshTeamsToken(accessToken, exchange, response, ipAddress);
                    }
                    return Mono.error(new UnauthorizedException(ErrorCode.H401I));
                })
                .switchIfEmpty(Mono.error(new UnauthorizedException(ErrorCode.H401I)))
                .flatMap(user -> chain.filter(exchange)
                        .contextWrite(context -> context
                                .put(UserContextHolder.USER_KEY, user)
                                .put(RequestContextHolder.CURRENT_REQUEST, request))
                );

    }

    private Mono<User> refreshTeamsToken(String expiredToken, ServerWebExchange exchange, ServerHttpResponse response, String ipAddress) {
        log.warn("Token expired");
        return teamsAuthUseCase.refreshToken(expiredToken)
                .flatMap(refreshedToken -> {
                    HttpHeaders headers = response.getHeaders();
                    headers.add("Refreshed-Token", refreshedToken.accessToken());
                    headers.setAccessControlExposeHeaders(List.of("Refreshed-Token"));
                    exchange.getAttributes().put("Access-Token", refreshedToken.accessToken());
                    return Mono.just(refreshedToken);
                })
                .flatMap(token -> teamsAuthUseCase.authorize(token.accessToken(), ipAddress));
    }
}
