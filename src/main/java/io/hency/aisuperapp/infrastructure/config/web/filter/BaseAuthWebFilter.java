package io.hency.aisuperapp.infrastructure.config.web.filter;

import io.hency.aisuperapp.common.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;

import static io.hency.aisuperapp.infrastructure.config.web.filter.WebFilterConstants.AUTH_EXCLUDE_URL_LIST;
import static io.hency.aisuperapp.infrastructure.config.web.filter.WebFilterConstants.TID;


@Slf4j
@RequiredArgsConstructor
public abstract class BaseAuthWebFilter implements WebFilter {
    protected final PathMatcher pathMatcher;

    protected boolean isOptionsMethod(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();

        if (request.getMethod() == HttpMethod.OPTIONS) {
            response.setStatusCode(HttpStatus.OK);
            return true;
        }
        return false;
    }

    protected boolean isExcludeUrl(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        String requestUrl = request.getPath().value();

        return AUTH_EXCLUDE_URL_LIST.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, requestUrl));
    }

    protected String getTenantId(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        String tid = HttpUtils.extractTenantId(request.getHeaders());
        exchange.getAttributes().put(TID, tid);
        return tid;
    }
}
