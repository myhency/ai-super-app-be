package io.hency.aisuperapp.features.chat.adapter.in;

import io.hency.aisuperapp.features.chat.adapter.in.dto.*;
import io.hency.aisuperapp.features.chat.application.port.in.ChatUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatUseCase chatUseCase;

    @PostMapping("/threads")
    public Mono<ChatThreadResponse> createThread(@Valid @RequestBody CreateThreadRequest request) {
        log.info("Creating thread: {}", request.getTitle());
        return chatUseCase.createThread(request.getUserId(), request.getTitle(), request.getModelName())
                .map(ChatThreadResponse::from);
    }

    @GetMapping("/threads/{threadId}")
    public Mono<ChatThreadResponse> getThread(@PathVariable Long threadId) {
        return chatUseCase.getThread(threadId)
                .map(ChatThreadResponse::from);
    }

    @GetMapping("/users/{userId}/threads")
    public Flux<ChatThreadResponse> getUserThreads(@PathVariable Long userId) {
        return chatUseCase.getUserThreads(userId)
                .map(ChatThreadResponse::from);
    }

    @GetMapping("/threads/{threadId}/messages")
    public Flux<MessageResponse> getThreadMessages(@PathVariable Long threadId) {
        return chatUseCase.getThreadMessages(threadId)
                .map(MessageResponse::from);
    }

    @PostMapping("/messages")
    public Mono<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        log.info("Sending message to thread: {}", request.getThreadId());
        return chatUseCase.sendMessage(request.getThreadId(), request.getContent(), request.getStream())
                .map(MessageResponse::from);
    }

    @PostMapping(value = "/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> sendMessageStream(@Valid @RequestBody SendMessageRequest request) {
        log.info("Sending streaming message to thread: {}", request.getThreadId());
        // TODO: Implement proper streaming
        return chatUseCase.sendMessage(request.getThreadId(), request.getContent(), true)
                .flatMapMany(msg -> Flux.just("data: " + msg.getContent() + "\n\n"));
    }
}
