package io.hency.aisuperapp.features.mcp.infrastructure.config;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class McpServerTools {
    public static void registerCalculationTool(McpAsyncServer mcpServer) {
        McpServerFeatures.AsyncToolSpecification calculationTool =
                new McpServerFeatures.AsyncToolSpecification(
                        new McpSchema.Tool("calculation", "Simple calculator", """
                                {
                                    "$schema": "http://json-schema.org/draft-07/schema#",
                                    "type": "object",
                                    "properties": {
                                        "operation": { "type": "string", "enum": ["add", "multiply"] },
                                        "a": { "type": "number" },
                                        "b": { "type": "number" }
                                    },
                                    "required": ["operation", "a", "b"]
                                }
                                """),
                        (exchange, params) -> {
                            try {
                                String operation = (String) params.get("operation");
                                Number a = (Number) params.get("a");
                                Number b = (Number) params.get("b");

                                double result = "add".equals(operation)
                                        ? a.doubleValue() + b.doubleValue()
                                        : a.doubleValue() * b.doubleValue();

                                log.info("계산 결과: {} {} {} = {}", a, operation, b, result);

                                return Mono.just(new McpSchema.CallToolResult(
                                        List.of(new McpSchema.TextContent("Result: " + result)),
                                        null
                                ));
                            } catch (Exception e) {
                                log.error("도구 실행 중 오류", e);
                                return Mono.error(e);
                            }
                        }
                );

        mcpServer.addTool(calculationTool)
                .doOnSuccess(v -> log.info("계산 도구 등록 완료"))
                .subscribe();
    }

    public static void registerDividerTool(McpAsyncServer mcpServer) {
        McpServerFeatures.AsyncToolSpecification calculationTool =
                new McpServerFeatures.AsyncToolSpecification(
                        new McpSchema.Tool("subtractDivide", "Simple calculator", """
                                {
                                    "$schema": "http://json-schema.org/draft-07/schema#",
                                    "type": "object",
                                    "properties": {
                                        "operation": { "type": "string", "enum": ["subtract", "divide"] },
                                        "a": { "type": "number" },
                                        "b": { "type": "number" }
                                    },
                                    "required": ["operation", "a", "b"]
                                }
                                """),
                        (exchange, params) -> {
                            try {
                                String operation = (String) params.get("operation");
                                Number a = (Number) params.get("a");
                                Number b = (Number) params.get("b");

                                double result;
                                if ("subtract".equals(operation)) {
                                    result = a.doubleValue() - b.doubleValue();
                                } else { // divide
                                    if (b.doubleValue() == 0) {
                                        return Mono.error(new IllegalArgumentException("Cannot divide by zero"));
                                    }
                                    result = a.doubleValue() / b.doubleValue();
                                }

                                log.info("계산 결과: {} {} {} = {}", a, operation, b, result);

                                return Mono.just(new McpSchema.CallToolResult(
                                        List.of(new McpSchema.TextContent("Result: " + result)),
                                        null
                                ));
                            } catch (Exception e) {
                                log.error("도구 실행 중 오류", e);
                                return Mono.error(e);
                            }
                        }
                );

        mcpServer.addTool(calculationTool)
                .doOnSuccess(v -> log.info("계산 도구 등록 완료"))
                .subscribe();
    }
}
