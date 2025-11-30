package io.hency.aisuperapp.features.mcp.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class McpRequest {
    @Builder.Default
    private String jsonrpc = "2.0";
    private String id;
    private String method;
    private Map<String, Object> params;
}
