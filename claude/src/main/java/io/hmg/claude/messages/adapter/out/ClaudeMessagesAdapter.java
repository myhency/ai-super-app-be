package io.hmg.claude.messages.adapter.out;

import io.hmg.claude.messages.application.port.out.ClaudeMessagesPort;
import io.hmg.claude.messages.application.vo.ClaudeModel;
import io.hmg.claude.messages.infrastructure.config.AwsBedrockProperties;
import io.hmg.claude.messages.infrastructure.external.BedrockMessagesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class ClaudeMessagesAdapter implements ClaudeMessagesPort {

    private final BedrockMessagesClient bedrockMessagesClient;
    private final AwsBedrockProperties awsBedrockProperties;

    @Override
    public Flux<?> sendChat(Object payload, ClaudeModel model) {
        var bedrockModel = awsBedrockProperties.getBedrock().getModels()
                .stream()
                .filter(m -> m.getName().equals(model.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Bedrock model not found: " + model.getName()));

        return bedrockMessagesClient.sendChat(payload, bedrockModel)
                .onErrorResume(Flux::error);
    }
}
