package io.hmg.claude.messages.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "external.aws")
public class AwsBedrockProperties {

    private Credentials credentials;
    private Bedrock bedrock;

    @Data
    public static class Credentials {
        private String accessKeyId;
        private String secretAccessKey;
    }

    @Data
    public static class Bedrock {
        private List<Model> models;
    }

    @Data
    public static class Model {
        private String name;
        private String modelId;
        private String region;
    }
}