package io.hency.aisuperapp.features.newmcp.infrastructure.config;

import io.hency.aisuperapp.features.newmcp.application.tools.CalculatorTool;
import io.hency.aisuperapp.features.newmcp.application.tools.ConfluenceCqlTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring AI MCP 서버 설정
 *
 * Spring AI의 Auto-configuration을 사용하여 자동으로 MCP 서버가 구성됨:
 * - @Tool 어노테이션이 붙은 메서드들이 ToolCallback로 변환됨
 * - WebFlux 기반 SSE 엔드포인트 자동 생성
 * - 기본 엔드포인트: /mcp/sse, /mcp/message
 *
 * application.yml에서 설정 가능:
 * spring:
 * ai:
 * mcp:
 * server:
 * name: "ai-super-app-newmcp-server"
 * version: "2.0.0"
 * type: ASYNC
 * sse-endpoint: "/newmcp/sse"
 * sse-message-endpoint: "/newmcp/message"
 */
@Slf4j
@Configuration
public class NewMcpServerConfig {

    /**
     * Calculator 도구를 MCP 도구로 등록
     */
    @Bean
    public ToolCallbackProvider calculatorTools(CalculatorTool calculatorTool) {
        log.info("Registering Calculator tools for MCP server");
        return MethodToolCallbackProvider.builder()
                .toolObjects(calculatorTool)
                .build();
    }

    /**
     * Confluence CQL 도구를 MCP 도구로 등록
     */
    @Bean
    public ToolCallbackProvider confluenceTools(ConfluenceCqlTool confluenceCqlTool) {
        log.info("Registering Confluence CQL tools for MCP server");
        return MethodToolCallbackProvider.builder()
                .toolObjects(confluenceCqlTool)
                .build();
    }
}