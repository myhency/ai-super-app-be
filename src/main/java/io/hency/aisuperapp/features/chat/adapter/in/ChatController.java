package io.hency.aisuperapp.features.chat.adapter.in;

import io.hency.aisuperapp.features.chat.adapter.in.dto.*;
import io.hency.aisuperapp.features.chat.application.port.in.ChatUseCase;
import io.hency.aisuperapp.features.file.infrastructure.repository.MessageFileAttachmentRepository;
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
    private final MessageFileAttachmentRepository messageFileAttachmentRepository;

    @PostMapping("/threads")
    public Mono<ChatThreadResponse> createThread(@Valid @RequestBody CreateThreadRequest request) {
        log.info("Creating thread: {}", request.getTitle());
        return chatUseCase.createThread(request.getUserId(), request.getTitle(), request.getModelName())
                .map(ChatThreadResponse::from);
    }

    @GetMapping("/threads/{threadId}")
    public Mono<ChatThreadResponse> getThread(@PathVariable("threadId") Long threadId) {
        return chatUseCase.getThread(threadId)
                .map(ChatThreadResponse::from);
    }

    @GetMapping("/users/{userId}/threads")
    public Flux<ChatThreadResponse> getUserThreads(@PathVariable("userId") Long userId) {
        return chatUseCase.getUserThreads(userId)
                .map(ChatThreadResponse::from);
    }

    @GetMapping("/threads/{threadId}/messages")
    public Flux<MessageResponse> getThreadMessages(@PathVariable("threadId") Long threadId) {
        return chatUseCase.getThreadMessages(threadId)
                .concatMap(message ->
                    messageFileAttachmentRepository.findFileAttachmentsByMessageId(message.getId())
                            .collectList()
                            .map(attachments -> MessageResponse.from(message, attachments))
                );
    }

    @PostMapping("/messages")
    public Mono<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        log.info("Sending message to thread: {}", request.getThreadId());
        return chatUseCase.sendMessage(request.getThreadId(), request.getContent(), request.getStream(), request.getFileIds())
                .map(MessageResponse::from);
    }

    @PostMapping(value = "/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<StreamChunkResponse> sendMessageStream(@Valid @RequestBody SendMessageRequest request) {
        log.info("Sending streaming message to thread: {}", request.getThreadId());
        return chatUseCase.sendMessageStream(request.getThreadId(), request.getContent(), request.getFileIds())
                .map(StreamChunkResponse::chunk)
                .concatWith(Flux.just(StreamChunkResponse.done()));
    }
}
