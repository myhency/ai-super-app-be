package io.hency.aisuperapp.features.newchat.adapter.out;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.ChatNotFoundException;
import io.hency.aisuperapp.features.newchat.application.domain.entity.Chat;
import io.hency.aisuperapp.features.newchat.application.domain.entity.ChatEntity;
import io.hency.aisuperapp.features.newchat.application.domain.entity.Message;
import io.hency.aisuperapp.features.newchat.application.domain.entity.SendChatCommand;
import io.hency.aisuperapp.features.newchat.application.domain.enums.ChatRoleType;
import io.hency.aisuperapp.features.newchat.application.port.out.ChatPort;
import io.hency.aisuperapp.features.newchat.infrastructure.repository.NewChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewChatAdapter implements ChatPort {
    private final ChatClient chatClient;
    private final NewChatRepository chatRepository;

    @Override
    public Flux<Message> sendChat(List<Message> sendMessages) {
        var messages = convertToSpringAIMessages(sendMessages);

        return chatClient.prompt()
                .messages(messages)
                .stream()
                .content()
                .map(Message::aiMessage)
                .onErrorResume(error -> {
                    log.error("Error during Spring AI chat call", error);
                    return Flux.error(error);
                });
    }

    @Override
    public Mono<ChatEntity> saveUserChat(SendChatCommand sendChatCommand) {
        Ulid topicId = sendChatCommand.topicId();
        Ulid chatId = sendChatCommand.chatId();
        Ulid parentChatId = sendChatCommand.parentChatId();
        Ulid userId = sendChatCommand.userId();
        ChatRoleType role = ChatRoleType.USER;
        String content = sendChatCommand.content();

        log.info("[SendChat] user: {}, content: {}", userId, content);

        return this.saveChat(chatId, userId, topicId, parentChatId, role, content);
    }

    @Override
    public Mono<Void> saveAiChat(Flux<Message> sendChatFlux, ChatEntity userChatEntity, Ulid aiChatId, Ulid userId) {
        Ulid topicId = userChatEntity.getTopicId();
        Ulid parentChatId = userChatEntity.getUlid();
        ChatRoleType role = ChatRoleType.AI;

        return sendChatFlux
                .collectList()
                .flatMap(messages -> {
                    String content = messages.stream()
                            .map(Message::content)
                            .collect(Collectors.joining());

                    log.info("[SendChat-Result] user: {}, content: {}", userChatEntity.getCreatedBy(), content);

                    return this.saveChat(aiChatId, userId, topicId, parentChatId, role, content);
                })
                .then();
    }

    private Mono<ChatEntity> saveChat(Ulid aiChatId, Ulid userId, Ulid topicId, Ulid parentChatId, ChatRoleType role,
            String content) {
        return Mono.justOrEmpty(parentChatId)
                .flatMap(ulid -> findChatByParentChatId(ulid)
                        .switchIfEmpty(Mono.error(new ChatNotFoundException(ErrorCode.H400A))))
                .map(parentChatEntity -> ChatEntity.of(aiChatId, topicId, parentChatEntity.getUlid(), role, content,
                        userId))
                .switchIfEmpty(Mono.just(ChatEntity.of(aiChatId, topicId, null, role, content, userId)))
                .flatMap(chatRepository::save)
                .onErrorResume(error -> {
                    log.error("Error during saving chat, role: {}", role, error);
                    return Mono.error(error);
                });
    }

    @Override
    public Mono<ChatEntity> findChatByParentChatId(Ulid parentId) {
        return chatRepository.findByUlid(parentId);
    }

    @Override
    public Mono<Chat> findFirstChatByTopicId(Ulid topicId) {
        return chatRepository.findTopByTopicIdOrderById(topicId)
                .map(Chat::of)
                .switchIfEmpty(Mono.error(new ChatNotFoundException(ErrorCode.H400A)));
    }

    private List<org.springframework.ai.chat.messages.Message> convertToSpringAIMessages(List<Message> messages) {
        List<org.springframework.ai.chat.messages.Message> aiMessages = new ArrayList<>();

        for (Message message : messages) {
            org.springframework.ai.chat.messages.Message aiMessage = switch (message.role()) {
                case USER -> new UserMessage(message.content());
                case AI -> new AssistantMessage(message.content());
                case SYSTEM -> new SystemMessage(message.content());
            };
            aiMessages.add(aiMessage);
        }

        return aiMessages;
    }
}