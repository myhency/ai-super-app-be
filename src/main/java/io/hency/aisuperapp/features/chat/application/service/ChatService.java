package io.hency.aisuperapp.features.chat.application.service;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.features.chat.application.port.in.ChatUseCase;
import io.hency.aisuperapp.features.chat.application.port.out.ChatPort;
import io.hency.aisuperapp.features.chat.application.domain.entity.Chat;
import io.hency.aisuperapp.features.chat.application.domain.entity.ChatEntity;
import io.hency.aisuperapp.features.chat.application.domain.entity.Message;
import io.hency.aisuperapp.features.chat.application.domain.entity.SendChatCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService implements ChatUseCase {
    private static final String DEFAULT_SYSTEM_PROMPT = "사용자가 정보를 찾는 데 도움이 되는 AI 도우미입니다.";
    private final ChatPort chatPort;

    @Override
    public Flux<Chat> send(SendChatCommand sendChatCommand, String inputSystemPrompt) {
        var aiChatId = sendChatCommand.aiChatId();
        var userId = sendChatCommand.userId();
        List<Message> messages = createMessages(sendChatCommand, inputSystemPrompt);
        Flux<Message> sendChatFlux = chatPort.sendChat(messages).cache();

        return chatPort.saveUserChat(sendChatCommand)
                .flatMapMany(chatEntity ->
                        saveAiChat(chatEntity, sendChatFlux, aiChatId, userId)
                                .flatMapMany(messageFlux ->
                                        buildChat(sendChatCommand, chatEntity, messageFlux)
                                )
                );
    }

    @Override
    public Flux<Chat> reSend(Ulid chatUlid, Ulid aiChatUlid, Ulid userId, String tenantId, List<Message> previousMessages, String systemPrompt) {
        return null;
    }

    private Flux<Chat> buildChat(SendChatCommand sendChatCommand, ChatEntity chatEntity, Flux<Message> messageFlux) {
        return messageFlux
                .map(message -> Tuples.of(message, chatEntity.getUlid()))
                .map(tuple -> chat(tuple, sendChatCommand));
    }

    private Mono<Flux<Message>> saveAiChat(ChatEntity chatEntity, Flux<Message> sendChatFlux, Ulid aiChatId, Ulid userId) {
        return Mono.deferContextual(ctx -> {
            chatPort.saveAiChat(sendChatFlux, chatEntity, aiChatId, userId)
                    .contextWrite(ctx)
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();

            return Mono.just(sendChatFlux);
        });
    }

    private List<Message> createMessages(SendChatCommand command, String inputSystemPrompt) {
        String systemPrompt = (inputSystemPrompt == null) ? DEFAULT_SYSTEM_PROMPT : inputSystemPrompt;

        List<Message> messages = new ArrayList<>();
        messages.add(Message.systemMessage(systemPrompt));
        messages.addAll(command.previousMessages());
        messages.add(Message.userMessage(command.content()));

        return messages;
    }

    private Chat chat(Tuple2<Message, Ulid> tuple, SendChatCommand sendChatCommand) {
        Message message = tuple.getT1();
        Ulid parentId = tuple.getT2();
        return Chat.builder().id(sendChatCommand.aiChatId()).topicId(sendChatCommand.topicId()).parentId(parentId).message(message).createdAt(ZonedDateTime.now()).createdBy(sendChatCommand.userId().toString()).updatedAt(ZonedDateTime.now()).updatedBy(sendChatCommand.userId().toString()).build();
    }
}
