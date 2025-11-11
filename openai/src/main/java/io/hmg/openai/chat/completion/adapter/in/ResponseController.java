package io.hmg.openai.chat.completion.adapter.in;

import io.hmg.openai.chat.completion.adapter.in.dto.ChatCompletionRequest;
import io.hmg.openai.chat.completion.application.domain.vo.ChatCompletionModel;
import io.hmg.openai.chat.completion.application.port.in.ChatCompletionUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/openai")
public class ResponseController {

    private final ChatCompletionUseCase chatCompletionUseCase;

    @PostMapping(
            value = "/chat/completions",
            produces = {MediaType.TEXT_EVENT_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE}
    )
    public Object chatCompletions(
            @Valid @RequestBody ChatCompletionRequest request
    ) {
        try {
            log.info("Send chat completion request: {}", request);
            log.info(request.toString());
            var model = request.getModel();
            var chatCompletionModel = ChatCompletionModel.fromName(model);
            Object payload = request.toPayload();

            Flux<?> result = chatCompletionUseCase.chatCompletion(
                    payload,
                    chatCompletionModel
            )
                    .onErrorResume(throwable -> {
                        log.debug("Error occurred while processing chat completion result: {}", throwable.getLocalizedMessage());
                        return Flux.error(throwable);
                    });

            if (request.getStream()) {
                return result;
            } else {
                return result.next();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
