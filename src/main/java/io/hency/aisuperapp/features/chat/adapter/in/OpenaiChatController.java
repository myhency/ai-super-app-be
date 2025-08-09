package io.hency.aisuperapp.features.chat.adapter.in;

import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.azure.openai.AzureOpenAiChatModel;
import org.springframework.ai.azure.openai.AzureOpenAiChatOptions;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/openai/chat")
public class OpenaiChatController {

    @PostMapping(produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ChatResponse> openaiChat() {
        var openAIClientBuilder = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential("48cfbe2f9e2f4a8895aa4a11073110bc"))
                .endpoint("https://h-chat.openai.azure.com");

        var openAIChatOptions = AzureOpenAiChatOptions.builder()
                .deploymentName("h-chat-gpt-4o-mini")
                .temperature(0.4)
                .maxTokens(1000)
                .build();

        var chatModel = AzureOpenAiChatModel.builder()
                .openAIClientBuilder(openAIClientBuilder)
                .defaultOptions(openAIChatOptions)
                .build();

        return chatModel.stream(
                new Prompt("generate the names of 5 famous pirates")
        );
    }
}
