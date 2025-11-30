package io.hency.aisuperapp.features.mcp.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.WebFluxSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static io.hency.aisuperapp.features.mcp.infrastructure.config.McpServerTools.registerCalculationTool;
import static io.hency.aisuperapp.features.mcp.infrastructure.config.McpServerTools.registerDividerTool;

//@Slf4j
//@Configuration
public class McpServerConfig {
    private static final String SSE_ENDPOINT = "/sse";
    private static final String MESSAGE_ENDPOINT = "/message";

    // @Bean
    public WebFluxSseServerTransportProvider sseServerTransport() {
        ObjectMapper objectMapper = new ObjectMapper();
        return new WebFluxSseServerTransportProvider(
                objectMapper,
                MESSAGE_ENDPOINT,
                SSE_ENDPOINT);
    }

    // @Bean
    public RouterFunction<?> mcpRouterFunction(WebFluxSseServerTransportProvider transportProvider) {
        if (transportProvider != null) {
            RouterFunction<?> routerFunction = transportProvider.getRouterFunction();
            // log.info("MCP 라우터 함수 등록됨");
            return RouterFunctions.route()
                    .add((RouterFunction<ServerResponse>) routerFunction)
                    .build();
        } else {
            // log.error("transportProvider가 null입니다. MCP 서버가 초기화되지 않았습니다.");
            return RouterFunctions.route().build();
        }
    }

    // @Bean
    public McpAsyncServer mcpServer(WebFluxSseServerTransportProvider transportProvider) {
        try {
            // 서버 인스턴스 생성
            McpAsyncServer server = McpServer.async(transportProvider)
                    .serverInfo("ai-super-app-server", "1.0.0")
                    .capabilities(McpSchema.ServerCapabilities.builder()
                            .tools(true)
                            .logging()
                            .build())
                    .build();

            registerCalculationTool(server);
            registerDividerTool(server);

            // log.info("MCP 서버가 초기화되었습니다");
            return server;
        } catch (Exception e) {
            // log.error("MCP 서버 초기화 중 오류 발생", e);
            throw new RuntimeException("MCP 서버 초기화 실패", e);
        }
    }
}
