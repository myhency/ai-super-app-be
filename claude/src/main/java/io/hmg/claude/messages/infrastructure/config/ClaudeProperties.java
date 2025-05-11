package io.hmg.claude.messages.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "external.anthropic.claude.sonnet")
public class ClaudeProperties {

    private List<Resource> resources;

    @Data
    public static class Resource {
        private String model;
        private String location;
        private String apiVersion;
        private String projectId;
        private String anthropicVersion;
    }
}
