package io.hency.aisuperapp.features.newchat.application.port.in;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.features.newchat.application.domain.entity.Chat;
import io.hency.aisuperapp.features.newchat.application.domain.entity.Message;
import io.hency.aisuperapp.features.newchat.application.domain.entity.SendChatCommand;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ChatUseCase {
    Flux<Chat> send(SendChatCommand sendChatCommand, String inputSystemPrompt);
    Flux<Chat> reSend(Ulid chatUlid, Ulid aiChatUlid, Ulid userId, String tenantId, List<Message> previousMessages, String systemPrompt);
}