package io.hency.aisuperapp.features.mcp.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "mcp.client")
public class McpClientProperties {

    private Map<String, McpServerConfig> servers = new HashMap<>();

    @Data
    public static class McpServerConfig {
        private String type; // docker, http, etc.
        private String containerId; // Docker container ID (for docker type with stdio)
        private String containerName; // Docker container name (preferred over containerId)
        private String host;
        private Integer port;
        private String transport; // sse, stdio
        private String endpoint;

        public String getBaseUrl() {
            if (port != null) {
                return String.format("http://%s:%d", host, port);
            }
            return String.format("http://%s", host);
        }

        /**
         * Get container identifier (name takes priority over ID)
         */
        public String getContainerIdentifier() {
            return containerName != null ? containerName : containerId;
        }
    }
}
