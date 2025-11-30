package io.hency.aisuperapp.features.chat.application.port.out;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.features.chat.application.domain.entity.Chat;
import io.hency.aisuperapp.features.chat.application.domain.entity.ChatEntity;
import io.hency.aisuperapp.features.chat.application.domain.entity.Message;
import io.hency.aisuperapp.features.chat.application.domain.entity.SendChatCommand;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ChatPort {
    Flux<Message> sendChat(List<Message> sendMessages);
    Mono<ChatEntity> saveUserChat(SendChatCommand sendChatCommand);
    Mono<Void> saveAiChat(Flux<Message> sendChatFlux, ChatEntity userChatEntity, Ulid aiChatId, Ulid userId);
    Mono<ChatEntity> findChatByParentChatId(Ulid parentId);
    Mono<Chat> findFirstChatByTopicId(Ulid topicId);
}
