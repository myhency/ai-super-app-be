package io.hency.aisuperapp.features.chat.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "external.azure.open-ai")
public class AzureOpenAIConfig {
    private List<AzureSubscriptions> azureSubscriptions;

    @Data
    public static class AzureSubscriptions {
        private String companyCode;
        private String subscribeId;
        private List<ApiResource> resources;
    }

    @Data
    public static class ApiResource {
        private Integer maxToken;
        private String model;
        private String url;
        private String apiKey;
        private String deploymentId;
    }
}
