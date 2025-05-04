package io.hency.aisuperapp.features.openai.chat.completion.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OpenAiWebClientConfig {
    @Bean("OpenAiChatCompletionClient")
    public Map<String, WebClient> openAiChatCompletionClient(OpenaiProperties properties) {
        ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(20*1024*1024))
                .build();
        return buildWebClients(properties, strategies);
    }

    private Map<String, WebClient> buildWebClients(OpenaiProperties properties, ExchangeStrategies strategies) {
        Map<String, WebClient> webClients = new HashMap<>();

        properties.getResources().forEach(resources -> {
            WebClient client = WebClient.builder()
                    .baseUrl(resources.getUrl())
                    .exchangeStrategies(strategies)
                    .build();
            webClients.put(resources.getModel(), client);
        });

        return webClients;
    }
}
