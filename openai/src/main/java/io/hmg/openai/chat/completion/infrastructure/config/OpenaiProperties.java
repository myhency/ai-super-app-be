package io.hmg.openai.chat.completion.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "external.azure.openai.chat.completion")
public class OpenaiProperties {
    private List<Resources> resources;

    @Data
    public static class Resources {
        private String model;
        private String url;
        private String apiKey;
        private String deploymentId;
        private String apiVersion;
    }
}
