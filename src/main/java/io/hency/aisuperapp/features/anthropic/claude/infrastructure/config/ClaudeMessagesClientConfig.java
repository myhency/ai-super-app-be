package io.hency.aisuperapp.features.anthropic.claude.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ClaudeMessagesClientConfig {

    @Bean("ClaudeMessagesClient")
    public Map<String, WebClient> ClaudeMessagesClient(
            ClaudeProperties properties
    ) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
                .build();

        return buildWebClients(properties, strategies);
    }

    private Map<String, WebClient> buildWebClients(
            ClaudeProperties properties,
            ExchangeStrategies strategies
    ) {
        Map<String, WebClient> webClients = new HashMap<>();

        properties.getResources().forEach(resource -> {

            WebClient client = WebClient.builder()
                    .baseUrl("https://" + resource.getLocation() + "-aiplatform.googleapis.com/v1/projects/" + resource.getProjectId() + "/locations/" + resource.getLocation() + "/publishers/anthropic/models")
                    .exchangeStrategies(strategies)
                    .build();
            webClients.put(resource.getModel(), client);
        });

        return webClients;
    }
}
