package io.hency.aisuperapp.features.openai.chat.completion.adapter.in;

import io.hency.aisuperapp.features.openai.chat.completion.adapter.in.dto.ChatCompletionRequest;
import io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo.ChatCompletionModel;
import io.hency.aisuperapp.features.openai.chat.completion.application.domain.vo.ChatCompletionPayload;
import io.hency.aisuperapp.features.openai.chat.completion.application.port.in.ChatCompletionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v2")
public class ChatCompletionController {

    private final ChatCompletionUseCase chatCompletionUseCase;

    @PostMapping(value = "/openai/deployments/{model}/chat/completions")
    public Object chatCompletions(
            @PathVariable("model") String model,
            @Valid @RequestBody ChatCompletionRequest request,
            ServerHttpResponse response
    ) {
        try {
            log.debug(request.toString());
            var chatCompletionModel = ChatCompletionModel.fromName(model);
            ChatCompletionPayload payload;

            if (chatCompletionModel.getModelType().equals(ChatCompletionModel.ModelType.GPT_SERIES)) {
                 payload = request.toGptSeriesPayload();
            } else if (chatCompletionModel.getModelType().equals(ChatCompletionModel.ModelType.O_SERIES)) {
                payload = request.toOSeriesPayload(chatCompletionModel);
            } else {
                throw new RuntimeException();
            }

            Flux<?> result = chatCompletionUseCase.chatCompletion(
                    payload,
                    chatCompletionModel
            )
                    .onErrorResume(throwable -> {
                        log.debug("Error occurred while processing chat completion result: {}", throwable.getLocalizedMessage());
                        return Flux.error(throwable);
                    });

            if (request.getStream()) {
                response.getHeaders().setContentType(MediaType.TEXT_EVENT_STREAM);
                return result;
            } else {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return result.next();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
