package io.hency.aisuperapp.features.chat.application.service;

import io.hency.aisuperapp.features.chat.application.domain.entity.ChatThread;
import io.hency.aisuperapp.features.chat.application.domain.entity.Message;
import io.hency.aisuperapp.features.chat.application.domain.vo.MessageRole;
import io.hency.aisuperapp.features.chat.application.port.in.ChatUseCase;
import io.hency.aisuperapp.features.chat.application.port.out.LlmPort;
import io.hency.aisuperapp.features.chat.infrastructure.repository.ChatThreadRepository;
import io.hency.aisuperapp.features.chat.infrastructure.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService implements ChatUseCase {

    private final ChatThreadRepository chatThreadRepository;
    private final MessageRepository messageRepository;
    private final LlmPort llmPort;

    @Override
    public Mono<ChatThread> createThread(Long userId, String title, String modelName) {
        ChatThread thread = ChatThread.builder()
                .userId(userId)
                .title(title)
                .modelName(modelName)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        return chatThreadRepository.save(thread)
                .doOnSuccess(t -> log.info("Created chat thread: {}", t.getId()));
    }

    @Override
    public Mono<ChatThread> getThread(Long threadId) {
        log.info("Getting thread: {}", threadId);
        return chatThreadRepository.findByIdAndIsDeletedFalse(threadId)
                .doOnNext(thread -> log.info("Thread found: id={}, model={}", thread.getId(), thread.getModelName()))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Thread not found: {}", threadId);
                    return Mono.error(new RuntimeException("Thread not found: " + threadId));
                }));
    }

    @Override
    public Flux<ChatThread> getUserThreads(Long userId) {
        return chatThreadRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    public Flux<Message> getThreadMessages(Long threadId) {
        return messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId);
    }

    @Override
    public Mono<Message> sendMessage(Long threadId, String content, Boolean stream) {
        log.info("sendMessage called: threadId={}, content length={}, stream={}", threadId, content.length(), stream);
        // Save user message
        Message userMessage = Message.builder()
                .threadId(threadId)
                .role(MessageRole.USER)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        return messageRepository.save(userMessage)
                .doOnSuccess(msg -> log.info("User message saved: id={}", msg.getId()))
                .flatMap(savedUserMsg -> {
                    log.info("Getting conversation history for thread: {}", threadId);
                    // Get conversation history
                    return getThreadMessages(threadId)
                            .doOnNext(msg -> log.info("Message in history: id={}, role={}", msg.getId(), msg.getRole()))
                            .doOnError(err -> log.error("Error fetching messages", err))
                            .doOnComplete(() -> log.info("Finished fetching messages"))
                            .collectList()
                            .doOnSuccess(messages -> log.info("Collected {} messages", messages.size()))
                            .doOnError(err -> log.error("Error collecting messages", err))
                            .flatMap(messages -> {
                                // Get thread to know model name
                                return getThread(threadId)
                                        .flatMap(thread -> {
                                            log.info("Found thread: {}, model: {}", thread.getId(), thread.getModelName());
                                            // Build messages for LLM
                                            List<Map<String, String>> llmMessages = messages.stream()
                                                    .map(msg -> Map.of(
                                                            "role", msg.getRole().name().toLowerCase(),
                                                            "content", msg.getContent()
                                                    ))
                                                    .collect(Collectors.toList());
                                            log.info("Built {} messages for LLM", llmMessages.size());

                                            // Call LLM
                                            if (Boolean.TRUE.equals(stream)) {
                                                // For now, we'll just get the first response
                                                // TODO: Implement proper streaming
                                                return llmPort.sendMessageStream(thread.getModelName(), llmMessages, 4096)
                                                        .doOnNext(chunk -> log.debug("Received stream chunk: {}", chunk))
                                                        .collectList()
                                                        .doOnSuccess(chunks -> log.debug("Collected {} chunks", chunks.size()))
                                                        .map(chunks -> {
                                                            String joined = String.join("", chunks);
                                                            log.debug("Joined response length: {}, content: {}", joined.length(), joined);
                                                            return joined;
                                                        })
                                                        .flatMap(response -> saveAssistantMessage(threadId, response));
                                            } else {
                                                return llmPort.sendMessage(thread.getModelName(), llmMessages, 4096)
                                                        .flatMap(response -> saveAssistantMessage(threadId, response));
                                            }
                                        });
                            });
                });
    }

    private Mono<Message> saveAssistantMessage(Long threadId, String content) {
        Message assistantMessage = Message.builder()
                .threadId(threadId)
                .role(MessageRole.ASSISTANT)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        return messageRepository.save(assistantMessage)
                .doOnSuccess(msg -> log.info("Saved assistant message: {}", msg.getId()));
    }
}
