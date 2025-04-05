package io.hency.aisuperapp.features.mcp.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Slf4j
@Configuration
public class McpServerConfig {
    private static final String SSE_ENDPOINT = "/sse";
    private static final String MESSAGE_ENDPOINT = "/message";

    @Bean
    public WebFluxSseServerTransportProvider sseServerTransport() {
        ObjectMapper objectMapper = new ObjectMapper();
        return new WebFluxSseServerTransportProvider(
                objectMapper,
                MESSAGE_ENDPOINT,
                SSE_ENDPOINT
        );
    }

    @Bean
    public RouterFunction<ServerResponse> mcpRouterFunction(WebFluxSseServerTransportProvider transportProvider) {
        if (transportProvider != null) {
            RouterFunction<?> routerFunction = transportProvider.getRouterFunction();
            log.info("MCP 라우터 함수 등록됨");
            return RouterFunctions.route()
                    .add((RouterFunction<ServerResponse>) routerFunction)
                    .build();
        } else {
            log.error("transportProvider가 null입니다. MCP 서버가 초기화되지 않았습니다.");
            return RouterFunctions.route().build();
        }
    }
}
