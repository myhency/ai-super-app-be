package io.hency.aisuperapp.features.mcp.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpResponse {
    private String jsonrpc;
    private String id;
    private Map<String, Object> result;
    private McpError error;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class McpError {
        private Integer code;
        private String message;
        private Object data;
    }
}
