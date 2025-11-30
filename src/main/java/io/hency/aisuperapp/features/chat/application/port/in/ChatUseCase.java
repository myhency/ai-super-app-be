package io.hency.aisuperapp.features.chat.application.port.in;

import io.hency.aisuperapp.features.chat.application.domain.entity.ChatThread;
import io.hency.aisuperapp.features.chat.application.domain.entity.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatUseCase {

    Mono<ChatThread> createThread(Long userId, String title, String modelName);

    Mono<ChatThread> getThread(Long threadId);

    Flux<ChatThread> getUserThreads(Long userId);

    Flux<Message> getThreadMessages(Long threadId);

    Mono<Message> sendMessage(Long threadId, String content, Boolean stream);
}
