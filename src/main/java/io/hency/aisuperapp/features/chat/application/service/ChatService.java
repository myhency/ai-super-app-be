package io.hency.aisuperapp.features.chat.application.service;

import io.hency.aisuperapp.features.chat.application.domain.entity.ChatThread;
import io.hency.aisuperapp.features.chat.application.domain.entity.Message;
import io.hency.aisuperapp.features.chat.application.domain.vo.MessageRole;
import io.hency.aisuperapp.features.chat.application.port.in.ChatUseCase;
import io.hency.aisuperapp.features.chat.application.port.out.LlmPort;
import io.hency.aisuperapp.features.chat.application.util.MessageConverter;
import io.hency.aisuperapp.features.chat.infrastructure.repository.ChatThreadRepository;
import io.hency.aisuperapp.features.chat.infrastructure.repository.MessageRepository;
import io.hency.aisuperapp.features.file.application.domain.entity.MessageFileAttachment;
import io.hency.aisuperapp.features.file.infrastructure.repository.MessageFileAttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService implements ChatUseCase {

    private final ChatThreadRepository chatThreadRepository;
    private final MessageRepository messageRepository;
    private final MessageFileAttachmentRepository messageFileAttachmentRepository;
    private final MessageConverter messageConverter;
    private final LlmPort llmPort;
    private final io.hency.aisuperapp.features.mcp.application.service.McpToolService mcpToolService;
    private final ToolUseHandler toolUseHandler;

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
    public Mono<Message> sendMessage(Long threadId, String content, Boolean stream, List<Long> fileIds, List<String> mcpTools) {
        log.info("sendMessage called: threadId={}, content length={}, stream={}, fileIds={}, mcpTools={}",
            threadId, content.length(), stream, fileIds, mcpTools);

        // Save user message
        Message userMessage = Message.builder()
                .threadId(threadId)
                .role(MessageRole.user)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        return messageRepository.save(userMessage)
                .doOnSuccess(msg -> log.info("User message saved: id={}", msg.getId()))
                .flatMap(savedUserMsg -> {
                    // Save file attachments if present
                    if (fileIds != null && !fileIds.isEmpty()) {
                        return Flux.fromIterable(fileIds)
                                .flatMap(fileId -> {
                                    MessageFileAttachment attachment = MessageFileAttachment.builder()
                                            .messageId(savedUserMsg.getId())
                                            .fileAttachmentId(fileId)
                                            .createdAt(LocalDateTime.now())
                                            .build();
                                    return messageFileAttachmentRepository.save(attachment);
                                })
                                .then(Mono.just(savedUserMsg));
                    }
                    return Mono.just(savedUserMsg);
                })
                .flatMap(savedUserMsg -> {
                    log.info("Getting conversation history for thread: {}", threadId);
                    // Get conversation history with attachments
                    return getThreadMessages(threadId)
                            .flatMap(msg ->
                                messageFileAttachmentRepository.findFileAttachmentsByMessageId(msg.getId())
                                        .collectList()
                                        .flatMap(files -> messageConverter.convertToLlmMessage(msg, files))
                            )
                            .collectList()
                            .flatMap(llmMessages -> {
                                log.info("Built {} messages for LLM", llmMessages.size());
                                // Get thread to know model name
                                return getThread(threadId)
                                        .flatMap(thread -> {
                                            log.info("Found thread: {}, model: {}", thread.getId(), thread.getModelName());

                                            // Check if MCP tools are requested
                                            if (mcpTools != null && !mcpTools.isEmpty()) {
                                                log.info("MCP tools requested: {}", mcpTools);
                                                String mcpServerName = mcpTools.get(0); // 첫 번째 서버 사용
                                                return mcpToolService.getToolDefinitions(mcpTools)
                                                        .flatMap(tools -> {
                                                            log.info("Got {} tool definitions", tools.size());
                                                            // Call LLM with tools
                                                            return llmPort.sendMessageWithTools(thread.getModelName(), llmMessages, 4096, tools)
                                                                    .flatMap(response -> toolUseHandler.handleToolUseResponse(
                                                                            thread.getModelName(), llmMessages, response, tools, mcpServerName))
                                                                    .flatMap(finalResponse -> saveAssistantMessage(threadId, finalResponse));
                                                        });
                                            } else {
                                                // Call LLM without tools (기존 로직)
                                                if (Boolean.TRUE.equals(stream)) {
                                                    return llmPort.sendMessageStream(thread.getModelName(), llmMessages, 4096)
                                                            .doOnNext(chunk -> log.debug("Received stream chunk: {}", chunk))
                                                            .collectList()
                                                            .map(chunks -> String.join("", chunks))
                                                            .flatMap(response -> saveAssistantMessage(threadId, response));
                                                } else {
                                                    return llmPort.sendMessage(thread.getModelName(), llmMessages, 4096)
                                                            .flatMap(response -> saveAssistantMessage(threadId, response));
                                                }
                                            }
                                        });
                            });
                });
    }

    @Override
    public Flux<String> sendMessageStream(Long threadId, String content, List<Long> fileIds, List<String> mcpTools) {
        log.info("sendMessageStream called: threadId={}, content length={}, fileIds={}, mcpTools={}",
            threadId, content.length(), fileIds, mcpTools);

        // Save user message
        Message userMessage = Message.builder()
                .threadId(threadId)
                .role(MessageRole.user)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        return messageRepository.save(userMessage)
                .doOnSuccess(msg -> log.info("User message saved: id={}", msg.getId()))
                .flatMap(savedUserMsg -> {
                    // Save file attachments if present
                    if (fileIds != null && !fileIds.isEmpty()) {
                        return Flux.fromIterable(fileIds)
                                .flatMap(fileId -> {
                                    MessageFileAttachment attachment = MessageFileAttachment.builder()
                                            .messageId(savedUserMsg.getId())
                                            .fileAttachmentId(fileId)
                                            .createdAt(LocalDateTime.now())
                                            .build();
                                    return messageFileAttachmentRepository.save(attachment);
                                })
                                .then(Mono.just(savedUserMsg));
                    }
                    return Mono.just(savedUserMsg);
                })
                .flatMapMany(savedUserMsg -> {
                    log.info("Getting conversation history for thread: {}", threadId);
                    // Get conversation history with attachments
                    return getThreadMessages(threadId)
                            .flatMap(msg ->
                                messageFileAttachmentRepository.findFileAttachmentsByMessageId(msg.getId())
                                        .collectList()
                                        .flatMap(files -> messageConverter.convertToLlmMessage(msg, files))
                            )
                            .collectList()
                            .flatMapMany(llmMessages -> {
                                log.info("Built {} messages for LLM", llmMessages.size());
                                // Get thread to know model name
                                return getThread(threadId)
                                        .flatMapMany(thread -> {
                                            log.info("Found thread: {}, model: {}", thread.getId(), thread.getModelName());

                                            // Check if MCP tools are requested
                                            if (mcpTools != null && !mcpTools.isEmpty()) {
                                                log.info("MCP tools requested in stream: {}. Processing with tool use.", mcpTools);
                                                String mcpServerName = mcpTools.get(0); // 첫 번째 서버 사용
                                                // MCP tool 실행 후 최종 응답은 스트리밍으로 처리
                                                return mcpToolService.getToolDefinitions(mcpTools)
                                                        .flatMapMany(tools -> {
                                                            log.info("Got {} tool definitions", tools.size());
                                                            return llmPort.sendMessageWithTools(thread.getModelName(), llmMessages, 4096, tools)
                                                                    .flatMapMany(response -> {
                                                                        // Tool use 스트리밍 처리
                                                                        Flux<String> streamFlux = toolUseHandler.handleToolUseResponseStream(
                                                                                thread.getModelName(), llmMessages, response, tools, mcpServerName);

                                                                        // 백그라운드에서 전체 메시지 저장
                                                                        streamFlux.cache()
                                                                                .reduce(new StringBuilder(), StringBuilder::append)
                                                                                .map(StringBuilder::toString)
                                                                                .flatMap(fullContent -> saveAssistantMessage(threadId, fullContent))
                                                                                .subscribe(
                                                                                        msg -> log.info("Assistant message saved successfully: {}", msg.getId()),
                                                                                        err -> log.error("Failed to save assistant message", err)
                                                                                );

                                                                        return streamFlux;
                                                                    });
                                                        });
                                            } else {
                                                // Call LLM with streaming and cache the stream
                                                Flux<String> streamFlux = llmPort.sendMessageStream(thread.getModelName(), llmMessages, 4096)
                                                        .doOnNext(chunk -> log.debug("Received stream chunk: {}", chunk))
                                                        .doOnError(err -> log.error("Error during streaming", err))
                                                        .cache();

                                                // Subscribe to save the complete message in background
                                                streamFlux
                                                        .reduce(new StringBuilder(), StringBuilder::append)
                                                        .map(StringBuilder::toString)
                                                        .flatMap(fullContent -> {
                                                            log.info("Stream complete. Saving assistant message with length: {}", fullContent.length());
                                                            return saveAssistantMessage(threadId, fullContent);
                                                        })
                                                        .subscribe(
                                                                msg -> log.info("Assistant message saved successfully: {}", msg.getId()),
                                                                err -> log.error("Failed to save assistant message", err)
                                                        );

                                                // Return the cached stream for client consumption
                                                return streamFlux;
                                            }
                                        });
                            });
                });
    }

    private Mono<Message> saveAssistantMessage(Long threadId, String content) {
        Message assistantMessage = Message.builder()
                .threadId(threadId)
                .role(MessageRole.assistant)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();

        return messageRepository.save(assistantMessage)
                .doOnSuccess(msg -> log.info("Saved assistant message: {}", msg.getId()));
    }
}
