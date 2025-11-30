package io.hency.aisuperapp.common.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

// import static io.hency.aisuperapp.common.infrastructure.config.web.filter.WebFilterConstants.TID;

public class HttpUtils {
    public static Mono<Void> redirect(URI uri, ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        response.getHeaders().setLocation(uri);
        return response.setComplete();
    }

    public static String extractTenantId(HttpHeaders httpHeaders) {
        // TID constant removed with auth module
        String tenantIdHeader = httpHeaders.getFirst("tid");

        if (tenantIdHeader == null || tenantIdHeader.trim().isEmpty()) {
            return "";
        }

        return tenantIdHeader;
    }

    public static String extractBearerToken(HttpHeaders httpHeaders) {
        String authorizationHeader = httpHeaders.getFirst(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || authorizationHeader.trim().isEmpty()) {
            return "";
        }

        return authorizationHeader.replace("Bearer ", "");
    }
}
