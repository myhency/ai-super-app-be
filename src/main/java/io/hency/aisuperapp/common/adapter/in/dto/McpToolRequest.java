package io.hency.aisuperapp.common.adapter.in.dto;

import lombok.Data;

@Data
public class McpToolRequest {
    private String toolName;
    private String operation;
    private Double a;
    private Double b;
}
