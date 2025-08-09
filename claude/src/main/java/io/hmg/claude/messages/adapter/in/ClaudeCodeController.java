package io.hmg.claude.messages.adapter.in;

import io.hmg.claude.messages.adapter.in.dto.AnthropicRequest;
import io.hmg.claude.messages.adapter.in.dto.ClaudeMessagesRequest;
import io.hmg.claude.messages.application.port.in.ClaudeMessagesUseCase;
import io.hmg.claude.messages.application.vo.ClaudeModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class ClaudeCodeController {

    private final ClaudeMessagesUseCase claudeMessagesUseCase;

    @PostMapping(value = "/messages")
    public Object messages(
            @Valid @RequestBody AnthropicRequest request,
            ServerHttpResponse response
    ) {
        try {
            log.info("Claude messages request: {} \n", request.toString());
            var model = ClaudeModel.fromName(request.getModel());
            Object payload = request.toPayload();

            Flux<?> result = claudeMessagesUseCase.messages(
                            payload,
                            model
                    )
                    .onErrorResume(throwable -> {
                        log.error("Error occurs while calling claude messages: {}", throwable.getLocalizedMessage());
                        return Flux.error(throwable);
                    });


            if (request.isStream()) {
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

