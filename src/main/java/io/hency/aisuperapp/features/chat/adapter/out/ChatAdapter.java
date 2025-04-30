package io.hency.aisuperapp.features.chat.adapter.out;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.ChatNotFoundException;
import io.hency.aisuperapp.features.chat.application.port.out.ChatPort;
import io.hency.aisuperapp.features.chat.application.domain.entity.Chat;
import io.hency.aisuperapp.features.chat.application.domain.entity.ChatEntity;
import io.hency.aisuperapp.features.chat.application.domain.entity.Message;
import io.hency.aisuperapp.features.chat.application.domain.entity.SendChatCommand;
import io.hency.aisuperapp.features.chat.application.domain.enums.ChatRoleType;
import io.hency.aisuperapp.infrastructure.client.OpenAIApiClient;
import io.hency.aisuperapp.infrastructure.client.dto.OpenAIApiClientRequest;
import io.hency.aisuperapp.infrastructure.config.azure.openai.AzureOpenAIConfig;
import io.hency.aisuperapp.infrastructure.repository.chat.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatAdapter implements ChatPort {
    private final OpenAIApiClient openAIApiClient;
    private final AzureOpenAIConfig azureOpenAIConfig;
    private final ChatRepository chatRepository;

    @Override
    public Flux<Message> sendChat(List<Message> sendMessages) {
        List<OpenAIApiClientRequest.ChatRequestMessage> chatRequestMessages = convertToChatRequestMessages(sendMessages);
        AzureOpenAIConfig.ApiResource resource = azureOpenAIConfig.getAzureSubscriptions().get(0).getResources().get(0);
        return openAIApiClient.sendMessage(chatRequestMessages, resource)
                .flatMap(openAIApiClientResponse ->
                        Flux.fromIterable(openAIApiClientResponse.getChoices())
                                .filter(choice ->
                                        choice.getDelta() != null && StringUtils.hasLength(choice.getDelta().getContent())
                                )
                                .map(choice -> choice.getDelta().getContent()))
                .map(Message::aiMessage)
                .onErrorResume(error -> {
                    log.error("Error during OpenAI API call", error);
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

    private Mono<ChatEntity> saveChat(Ulid aiChatId, Ulid userId, Ulid topicId, Ulid parentChatId, ChatRoleType role, String content) {
        return Mono.justOrEmpty(parentChatId)
                .flatMap(ulid -> findChatByParentChatId(ulid)
                        .switchIfEmpty(Mono.error(new ChatNotFoundException(ErrorCode.H400A)))
                )
                .map(parentChatEntity ->
                        ChatEntity.of(aiChatId, topicId, parentChatEntity.getUlid(), role, content, userId)
                )
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

    private List<OpenAIApiClientRequest.ChatRequestMessage> convertToChatRequestMessages(List<Message> sendMessages) {
        return sendMessages.stream()
                .map(this::toChatRequestMessage)
                .toList();
    }

    private OpenAIApiClientRequest.ChatRequestMessage toChatRequestMessage(Message message) {
        var role = switch (message.role()) {
            case USER -> "user";
            case AI -> "assistant";
            case SYSTEM -> "system";
        };

        OpenAIApiClientRequest.ChatRequestMessage.Content content = OpenAIApiClientRequest.ChatRequestMessage.Content.builder()
                .type("text")
                .text(message.content())
                .build();

        return OpenAIApiClientRequest.ChatRequestMessage.builder()
                .role(role)
                .content(List.of(content))
                .build();
    }
}
