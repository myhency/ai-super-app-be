package io.hency.aisuperapp.features.topic.application.domain.entity;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.domain.entity.BaseIdEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "topic")
public class TopicEntity extends BaseIdEntity {

    private Ulid userId;
    private String title;

    public static TopicEntity defaultTopic(Ulid userUlid) {
        final String defaultTitle = "New Chat";
        return new TopicEntity(userUlid, defaultTitle);
    }

    public void updateTitle(String title) {
        var _title = title;
        if (title.length() > 50) {
            _title = title.substring(0,50) + "...";
        }
        this.title = _title;
        updateTime();
    }
}
