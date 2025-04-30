package io.hency.aisuperapp.common.infrastructure.config.web.filter;

import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsProcessor;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.DefaultCorsProcessor;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Order(-2)
public class CorsFilter implements WebFilter {

    private final CorsConfigurationSource configSource;

    private final CorsProcessor processor;

    public CorsFilter(CorsConfigurationSource corsConfigurationSource) {
        this(corsConfigurationSource, new DefaultCorsProcessor());
    }

    public CorsFilter(CorsConfigurationSource configSource, CorsProcessor processor) {
        Assert.notNull(configSource, "CorsConfigurationSource must not be null");
        Assert.notNull(processor, "CorsProcessor must not be null");
        this.configSource = configSource;
        this.processor = processor;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        CorsConfiguration corsConfiguration = this.configSource.getCorsConfiguration(exchange);
        boolean isValid = this.processor.process(corsConfiguration, exchange);
        if (!isValid || CorsUtils.isPreFlightRequest(request)) {
            return Mono.empty();
        }
        return chain.filter(exchange);
    }
}
