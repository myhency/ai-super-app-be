package io.hency.aisuperapp.features.mcp.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hency.aisuperapp.features.mcp.infrastructure.client.dto.McpRequest;
import io.hency.aisuperapp.features.mcp.infrastructure.client.dto.McpResponse;
import io.hency.aisuperapp.features.mcp.infrastructure.client.dto.McpTool;
import io.hency.aisuperapp.features.mcp.infrastructure.config.McpClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class McpClient {

    private final McpClientProperties mcpClientProperties;
    private final ObjectMapper objectMapper;

    /**
     * MCP 서버로부터 사용 가능한 도구 목록을 가져옵니다.
     */
    public Mono<List<McpTool>> listTools(String serverName) {
        log.info("Listing tools from MCP server: {}", serverName);

        McpClientProperties.McpServerConfig serverConfig = mcpClientProperties.getServers().get(serverName);
        if (serverConfig == null) {
            return Mono.error(new IllegalArgumentException("MCP server not found: " + serverName));
        }

        McpRequest request = McpRequest.builder()
                .id(UUID.randomUUID().toString())
                .method("tools/list")
                .params(Map.of())
                .build();

        return executeDockerCommand(serverConfig.getContainerIdentifier(), request)
                .flatMap(response -> {
                    if (response.getError() != null) {
                        log.error("MCP error: {}", response.getError().getMessage());
                        return Mono.error(new RuntimeException("MCP error: " + response.getError().getMessage()));
                    }

                    try {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> toolsData = (List<Map<String, Object>>) response.getResult().get("tools");
                        List<McpTool> tools = toolsData.stream()
                                .map(toolData -> objectMapper.convertValue(toolData, McpTool.class))
                                .toList();
                        log.info("Retrieved {} tools from {}", tools.size(), serverName);
                        return Mono.just(tools);
                    } catch (Exception e) {
                        log.error("Failed to parse tools response", e);
                        return Mono.error(e);
                    }
                })
                .doOnError(error -> log.error("Error listing tools from {}: {}", serverName, error.getMessage()));
    }

    /**
     * MCP 서버의 특정 도구를 실행합니다.
     */
    public Mono<Map<String, Object>> callTool(String serverName, String toolName, Map<String, Object> arguments) {
        log.info("Calling tool '{}' on MCP server: {}", toolName, serverName);

        McpClientProperties.McpServerConfig serverConfig = mcpClientProperties.getServers().get(serverName);
        if (serverConfig == null) {
            return Mono.error(new IllegalArgumentException("MCP server not found: " + serverName));
        }

        McpRequest request = McpRequest.builder()
                .id(UUID.randomUUID().toString())
                .method("tools/call")
                .params(Map.of(
                        "name", toolName,
                        "arguments", arguments
                ))
                .build();

        return executeDockerCommand(serverConfig.getContainerIdentifier(), request)
                .flatMap(response -> {
                    if (response.getError() != null) {
                        log.error("MCP tool call error: {}", response.getError().getMessage());
                        return Mono.error(new RuntimeException("MCP tool call error: " + response.getError().getMessage()));
                    }

                    log.info("Tool '{}' executed successfully", toolName);
                    return Mono.just(response.getResult());
                })
                .doOnError(error -> log.error("Error calling tool {} on {}: {}", toolName, serverName, error.getMessage()));
    }

    /**
     * Docker exec를 통해 MCP 서버와 stdio로 통신
     */
    private Mono<McpResponse> executeDockerCommand(String containerId, McpRequest request) {
        return Mono.fromCallable(() -> {
            log.debug("Executing docker command on container: {}", containerId);

            // docker exec를 사용하여 mcp-server-fetch 실행
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "docker", "exec", "-i", containerId, "mcp-server-fetch"
            );
            processBuilder.redirectErrorStream(false);

            Process process = processBuilder.start();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

            try {
                // 1. Initialize 요청 전송
                McpRequest initRequest = McpRequest.builder()
                        .id("init-" + UUID.randomUUID().toString())
                        .method("initialize")
                        .params(Map.of(
                                "protocolVersion", "2024-11-05",
                                "capabilities", Map.of(),
                                "clientInfo", Map.of(
                                        "name", "ai-super-app",
                                        "version", "1.0.0"
                                )
                        ))
                        .build();

                String initJson = objectMapper.writeValueAsString(initRequest);
                log.debug("Sending initialize: {}", initJson);
                writer.write(initJson);
                writer.newLine();
                writer.flush();

                // Initialize 응답 읽기
                String initResponse = reader.readLine();
                log.debug("Received initialize response: {}", initResponse);

                // 2. Initialized 알림 전송
                Map<String, Object> initializedNotification = Map.of(
                        "jsonrpc", "2.0",
                        "method", "notifications/initialized",
                        "params", Map.of()
                );
                String initializedJson = objectMapper.writeValueAsString(initializedNotification);
                log.debug("Sending initialized notification: {}", initializedJson);
                writer.write(initializedJson);
                writer.newLine();
                writer.flush();

                // 3. 실제 요청 전송
                String requestJson = objectMapper.writeValueAsString(request);
                log.debug("Sending request: {}", requestJson);
                writer.write(requestJson);
                writer.newLine();
                writer.flush();

                // 응답 읽기
                String responseLine = reader.readLine();
                log.debug("Received response: {}", responseLine);

                if (responseLine == null || responseLine.isEmpty()) {
                    // 에러 스트림 확인
                    String errorLine = errorReader.readLine();
                    if (errorLine != null) {
                        log.error("Error from MCP server: {}", errorLine);
                    }
                    throw new RuntimeException("No response from MCP server");
                }

                McpResponse response = objectMapper.readValue(responseLine, McpResponse.class);

                return response;
            } finally {
                writer.close();
                reader.close();
                errorReader.close();
                process.destroy();
            }
        }).subscribeOn(Schedulers.boundedElastic())
          .doOnError(error -> log.error("Error executing docker command: {}", error.getMessage(), error));
    }
}
