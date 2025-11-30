package io.hmg.claude.messages.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AwsBedrockProperties.class)
public class AwsBedrockConfig {
}