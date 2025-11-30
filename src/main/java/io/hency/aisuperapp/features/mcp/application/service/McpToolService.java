package io.hency.aisuperapp.features.mcp.application.service;

import io.hency.aisuperapp.features.mcp.infrastructure.client.McpClient;
import io.hency.aisuperapp.features.mcp.infrastructure.client.dto.McpTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MCP 도구 관련 비즈니스 로직을 처리하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpToolService {

    private final McpClient mcpClient;

    /**
     * 여러 MCP 서버로부터 도구 정의를 가져와서 Claude가 이해할 수 있는 형식으로 변환
     */
    public Mono<List<Map<String, Object>>> getToolDefinitions(List<String> mcpServerNames) {
        if (mcpServerNames == null || mcpServerNames.isEmpty()) {
            return Mono.just(List.of());
        }

        return Flux.fromIterable(mcpServerNames)
                .flatMap(serverName ->
                        mcpClient.listTools(serverName)
                                .map(tools -> convertToClaudeToolDefinitions(serverName, tools))
                                .onErrorResume(error -> {
                                    log.error("Failed to get tools from {}: {}", serverName, error.getMessage());
                                    return Mono.just(List.<Map<String, Object>>of());
                                })
                )
                .flatMap(Flux::fromIterable)
                .collectList();
    }

    /**
     * MCP 도구를 실행하고 결과를 반환
     */
    public Mono<Map<String, Object>> executeTool(String serverName, String toolName, Map<String, Object> input) {
        log.info("Executing tool '{}' on server '{}'", toolName, serverName);

        return mcpClient.callTool(serverName, toolName, input)
                .map(result -> {
                    log.info("Tool '{}' executed successfully", toolName);
                    return result;
                })
                .onErrorResume(error -> {
                    log.error("Failed to execute tool '{}': {}", toolName, error.getMessage());
                    return Mono.just(Map.of(
                            "error", true,
                            "message", error.getMessage()
                    ));
                });
    }

    /**
     * MCP 도구를 Claude Tool Definition 형식으로 변환
     */
    private List<Map<String, Object>> convertToClaudeToolDefinitions(String serverName, List<McpTool> mcpTools) {
        List<Map<String, Object>> tools = mcpTools.stream()
                .map(tool -> {
                    Map<String, Object> claudeTool = new HashMap<>();
                    // Claude에게는 원본 도구 이름만 전달 (서버 정보는 description에 포함)
                    claudeTool.put("name", tool.getName());
                    // Description에 MCP 서버 정보 추가
                    String enhancedDescription = tool.getDescription() +
                        "\n\n[This tool is provided by MCP server: " + serverName + "]";
                    claudeTool.put("description", enhancedDescription);
                    claudeTool.put("input_schema", tool.getInputSchema());
                    log.info("Converted tool: name={}, server={}", tool.getName(), serverName);
                    return claudeTool;
                })
                .toList();
        log.info("Total converted tools: {}", tools.size());
        return tools;
    }

    /**
     * Claude의 tool_use에서 서버 이름과 도구 이름을 추출
     * 형식: "fetch@fetch" -> {serverName: "fetch", toolName: "fetch"}
     */
    public Map<String, String> parseToolName(String fullToolName) {
        String[] parts = fullToolName.split("@");
        if (parts.length == 2) {
            return Map.of(
                    "toolName", parts[0],
                    "serverName", parts[1]
            );
        }
        // 기본값
        return Map.of(
                "toolName", fullToolName,
                "serverName", "unknown"
        );
    }
}
