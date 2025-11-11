package io.hency.aisuperapp.features.newchat.infrastructure.config;

import lombok.Data;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;

import java.util.List;

@Configuration
public class SpringAIChatConfig {

    @Bean
    public AzureOpenAiChatModel azureOpenAiChatModel(AzureOpenAIProperties properties) {
        var subscription = properties.getAzureSubscriptions().get(0);
        var resource = subscription.getResources().get(0);

        var openAIClientBuilder = new OpenAIClientBuilder()
                .endpoint(resource.getUrl())
                .credential(new AzureKeyCredential(resource.getApiKey()));

        var options = AzureOpenAiChatOptions.builder()
                .deploymentName(resource.getDeploymentId())
                .maxTokens(resource.getMaxToken())
                .build();

        return AzureOpenAiChatModel.builder()
                .openAIClientBuilder(openAIClientBuilder)
                .defaultOptions(options)
                .build();
    }

    @Bean
    public ChatClient chatClient(AzureOpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel).build();
    }

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "external.azure.open-ai")
    public static class AzureOpenAIProperties {
        private List<AzureSubscription> azureSubscriptions;

        @Data
        public static class AzureSubscription {
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
}