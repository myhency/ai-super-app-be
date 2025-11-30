package io.hency.aisuperapp.features.chat.application.service;

import io.hency.aisuperapp.features.chat.application.port.out.LlmPort;
import io.hency.aisuperapp.features.mcp.application.service.McpToolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Claude의 tool_use를 처리하고 MCP 도구를 실행하는 핸들러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolUseHandler {

    private final McpToolService mcpToolService;
    private final LlmPort llmPort;

    private static final int MAX_TOOL_ITERATIONS = 5;

    // MCP 서버 이름을 저장 (tool_use 실행 시 사용)
    private String currentMcpServerName;

    /**
     * tool_use가 포함된 응답을 처리하고, 필요한 경우 도구를 실행하여 최종 응답을 반환
     */
    public Mono<String> handleToolUseResponse(
            String modelName,
            List<Map<String, Object>> conversationMessages,
            Map<String, Object> claudeResponse,
            List<Map<String, Object>> tools,
            String mcpServerName
    ) {
        log.info("Handling tool_use response with MCP server: {}", mcpServerName);
        this.currentMcpServerName = mcpServerName;

        // stop_reason이 tool_use인지 확인
        String stopReason = (String) claudeResponse.get("stop_reason");
        if (!"tool_use".equals(stopReason)) {
            // tool_use가 아니면 텍스트 추출하여 반환
            return extractTextFromResponse(claudeResponse);
        }

        // tool_use 처리 시작
        return processToolUseCycle(modelName, conversationMessages, claudeResponse, tools, 0);
    }

    /**
     * tool_use가 포함된 응답을 처리하고, 최종 응답을 스트리밍으로 반환
     */
    public Flux<String> handleToolUseResponseStream(
            String modelName,
            List<Map<String, Object>> conversationMessages,
            Map<String, Object> claudeResponse,
            List<Map<String, Object>> tools,
            String mcpServerName
    ) {
        log.info("Handling tool_use response with streaming, MCP server: {}", mcpServerName);
        this.currentMcpServerName = mcpServerName;

        // stop_reason이 tool_use인지 확인
        String stopReason = (String) claudeResponse.get("stop_reason");
        if (!"tool_use".equals(stopReason)) {
            // tool_use가 아니면 텍스트 추출하여 반환
            return extractTextFromResponse(claudeResponse).flux();
        }

        // tool_use 처리 시작 - 스트리밍 버전
        return processToolUseCycleStream(modelName, conversationMessages, claudeResponse, tools, 0);
    }

    /**
     * tool_use 순환 처리 (재귀적으로 tool_use를 처리)
     */
    private Mono<String> processToolUseCycle(
            String modelName,
            List<Map<String, Object>> messages,
            Map<String, Object> claudeResponse,
            List<Map<String, Object>> tools,
            int iteration
    ) {
        if (iteration >= MAX_TOOL_ITERATIONS) {
            log.warn("Max tool iterations reached");
            return Mono.just("Tool execution limit reached");
        }

        log.info("Tool use cycle iteration: {}", iteration);

        // Claude의 응답에서 content 추출
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> contentBlocks = (List<Map<String, Object>>) claudeResponse.get("content");

        // assistant 메시지 구성 (Claude의 응답을 그대로)
        Map<String, Object> assistantMessage = Map.of(
                "role", "assistant",
                "content", contentBlocks
        );

        // tool_use 블록 찾기 및 실행
        return executeToolUses(contentBlocks)
                .collectList()
                .flatMap(toolResults -> {
                    // user 메시지 구성 (tool_result 포함)
                    Map<String, Object> userMessage = Map.of(
                            "role", "user",
                            "content", toolResults
                    );

                    // 대화 히스토리에 assistant와 user 메시지 추가
                    List<Map<String, Object>> updatedMessages = new ArrayList<>(messages);
                    updatedMessages.add(assistantMessage);
                    updatedMessages.add(userMessage);

                    log.info("Sending tool results back to Claude");

                    // Claude에게 다시 요청
                    return llmPort.sendMessageWithTools(modelName, updatedMessages, 4096, tools)
                            .flatMap(newResponse -> {
                                String newStopReason = (String) newResponse.get("stop_reason");
                                if ("tool_use".equals(newStopReason)) {
                                    // 여전히 tool_use면 재귀적으로 처리
                                    return processToolUseCycle(modelName, updatedMessages, newResponse, tools, iteration + 1);
                                } else {
                                    // 최종 텍스트 응답 반환
                                    return extractTextFromResponse(newResponse);
                                }
                            });
                });
    }

    /**
     * tool_use 순환 처리 - 스트리밍 버전 (tool 실행 완료 후 마지막 응답만 스트리밍)
     */
    private Flux<String> processToolUseCycleStream(
            String modelName,
            List<Map<String, Object>> messages,
            Map<String, Object> claudeResponse,
            List<Map<String, Object>> tools,
            int iteration
    ) {
        if (iteration >= MAX_TOOL_ITERATIONS) {
            log.warn("Max tool iterations reached");
            return Flux.just("Tool execution limit reached");
        }

        log.info("Tool use cycle iteration (stream): {}", iteration);

        // Claude의 응답에서 content 추출
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> contentBlocks = (List<Map<String, Object>>) claudeResponse.get("content");

        // assistant 메시지 구성 (Claude의 응답을 그대로)
        Map<String, Object> assistantMessage = Map.of(
                "role", "assistant",
                "content", contentBlocks
        );

        // tool_use 블록 찾기 및 실행
        return executeToolUses(contentBlocks)
                .collectList()
                .flatMapMany(toolResults -> {
                    // user 메시지 구성 (tool_result 포함)
                    Map<String, Object> userMessage = Map.of(
                            "role", "user",
                            "content", toolResults
                    );

                    // 대화 히스토리에 assistant와 user 메시지 추가
                    List<Map<String, Object>> updatedMessages = new ArrayList<>(messages);
                    updatedMessages.add(assistantMessage);
                    updatedMessages.add(userMessage);

                    log.info("Sending tool results back to Claude (will stream final response)");

                    // Claude에게 다시 요청
                    return llmPort.sendMessageWithTools(modelName, updatedMessages, 4096, tools)
                            .flatMapMany(newResponse -> {
                                String newStopReason = (String) newResponse.get("stop_reason");
                                if ("tool_use".equals(newStopReason)) {
                                    // 여전히 tool_use면 재귀적으로 처리
                                    return processToolUseCycleStream(modelName, updatedMessages, newResponse, tools, iteration + 1);
                                } else {
                                    // 최종 응답 - 스트리밍으로 반환
                                    log.info("Tool use complete, streaming final response from Claude");
                                    return llmPort.sendMessageStream(modelName, updatedMessages, 4096);
                                }
                            });
                });
    }

    /**
     * content 블록들에서 tool_use를 찾아 실행
     */
    private Flux<Map<String, Object>> executeToolUses(List<Map<String, Object>> contentBlocks) {
        return Flux.fromIterable(contentBlocks)
                .filter(block -> "tool_use".equals(block.get("type")))
                .flatMap(toolUseBlock -> {
                    String toolUseId = (String) toolUseBlock.get("id");
                    String fullToolName = (String) toolUseBlock.get("name");
                    @SuppressWarnings("unchecked")
                    Map<String, Object> input = (Map<String, Object>) toolUseBlock.get("input");

                    log.info("Executing tool: {} with input: {}", fullToolName, input);

                    // 현재 설정된 MCP 서버 사용
                    String serverName = currentMcpServerName;
                    String toolName = fullToolName;

                    // MCP 도구 실행
                    return mcpToolService.executeTool(serverName, toolName, input)
                            .map(result -> {
                                // tool_result 형식으로 변환
                                return Map.<String, Object>of(
                                        "type", "tool_result",
                                        "tool_use_id", toolUseId,
                                        "content", formatToolResult(result)
                                );
                            })
                            .onErrorResume(error -> {
                                log.error("Tool execution failed: {}", error.getMessage());
                                return Mono.just(Map.<String, Object>of(
                                        "type", "tool_result",
                                        "tool_use_id", toolUseId,
                                        "content", "Error: " + error.getMessage(),
                                        "is_error", true
                                ));
                            });
                });
    }

    /**
     * MCP 도구 실행 결과를 Claude가 이해할 수 있는 형식으로 변환
     */
    private String formatToolResult(Map<String, Object> result) {
        // MCP 응답 형식: { "content": [ { "type": "text", "text": "..." } ] }
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
        if (content != null && !content.isEmpty()) {
            Map<String, Object> firstContent = content.get(0);
            if ("text".equals(firstContent.get("type"))) {
                return (String) firstContent.get("text");
            }
        }
        // 형식이 다르면 그대로 문자열로 변환
        return result.toString();
    }

    /**
     * Claude 응답에서 텍스트 추출
     */
    private Mono<String> extractTextFromResponse(Map<String, Object> response) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        if (content != null && !content.isEmpty()) {
            StringBuilder text = new StringBuilder();
            for (Map<String, Object> block : content) {
                if ("text".equals(block.get("type"))) {
                    text.append(block.get("text"));
                }
            }
            return Mono.just(text.toString());
        }
        return Mono.just("");
    }
}
