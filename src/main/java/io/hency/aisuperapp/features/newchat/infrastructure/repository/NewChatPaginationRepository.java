package io.hency.aisuperapp.features.newchat.infrastructure.repository;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.domain.enums.SliceDirection;
import io.hency.aisuperapp.features.newchat.application.domain.entity.ChatEntity;
import reactor.core.publisher.Flux;

public interface NewChatPaginationRepository {
    Flux<ChatEntity> findPagedChats(Ulid topicId, Ulid baseId, SliceDirection sliceDirection, int size);
}