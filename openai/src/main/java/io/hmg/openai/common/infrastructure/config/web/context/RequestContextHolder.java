package io.hmg.openai.common.infrastructure.config.web.context;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public class RequestContextHolder {
    public static final String CURRENT_REQUEST = "currentRequest";

    public static Mono<ServerHttpRequest> getRequest() {
        return Mono.deferContextual(contextView -> Mono.just(contextView.get(CURRENT_REQUEST)));
    }
}
