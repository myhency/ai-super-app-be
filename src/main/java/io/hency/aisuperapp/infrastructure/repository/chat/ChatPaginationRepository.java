package io.hency.aisuperapp.infrastructure.repository.chat;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.domain.enums.SliceDirection;
import io.hency.aisuperapp.features.chat.application.domain.entity.ChatEntity;
import reactor.core.publisher.Flux;

public interface ChatPaginationRepository {
    Flux<ChatEntity> findPagedChats(Ulid topicId, Ulid baseId, SliceDirection sliceDirection, int size);
}
