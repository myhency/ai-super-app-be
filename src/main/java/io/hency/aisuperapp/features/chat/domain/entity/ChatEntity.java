package io.hency.aisuperapp.features.chat.domain.entity;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.domain.entity.BaseIdEntity;
import io.hency.aisuperapp.features.chat.domain.enums.ChatRoleType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "chat")
public class ChatEntity extends BaseIdEntity {
    private Ulid topicId;
    private Ulid parentId;
    private ChatRoleType role;
    private String content;

    private ChatEntity(Ulid ulid, Ulid topicId, Ulid parentId, ChatRoleType role, String content, Ulid userId) {
        super(ulid, userId);
        this.topicId = topicId;
        this.parentId = parentId;
        this.role = role;
        this.content = content;
    }

    public static ChatEntity of(Ulid chatUlid, Ulid topicId, Ulid parentId, ChatRoleType role, String content, Ulid userId) {
        return new ChatEntity(chatUlid, topicId, parentId, role, content, userId);
    }
}
