package io.hency.aisuperapp.features.topic.application.service;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.constant.SystemPrompt;
import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.TopicNotFoundException;
// import io.hency.aisuperapp.features.chat.application.port.out.ChatPort; // Disabled old chat system
// import io.hency.aisuperapp.features.chat.application.domain.entity.Message; // Disabled old chat system
import io.hency.aisuperapp.features.topic.application.port.in.TopicUseCase;
import io.hency.aisuperapp.features.topic.application.port.out.TopicPort;
import io.hency.aisuperapp.features.topic.application.domain.entity.Topic;
import io.hency.aisuperapp.features.topic.application.domain.entity.TopicEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
// @RequiredArgsConstructor // Commented out due to disabled ChatPort
public class TopicService implements TopicUseCase {
    private final TopicPort topicPort;
    // private final ChatPort chatPort; // Disabled old chat system

    public TopicService(TopicPort topicPort) {
        this.topicPort = topicPort;
    }

    @Override
    public Mono<Topic> generate(Ulid topicId, Ulid userId) {
        // Disabled - requires old ChatPort and Message system
        return Mono.error(new UnsupportedOperationException("Topic generation temporarily disabled"));
    }

    @Override
    public Mono<Topic> createDefaultTopic(Ulid topicId, Ulid userId) {
        return topicPort.findTopicByTopicIdAndUserId(topicId, userId)
                .switchIfEmpty(Mono.defer(() -> {
                    TopicEntity topic = TopicEntity.defaultTopic(userId);
                    return topicPort.save(topic);
                }))
                .map(Topic::of);
    }

    // Disabled - requires old Message system
    // private List<Message> createMessages(String userMessage) {
    //     String systemPrompt = SystemPrompt.SUMMARIZE_USER_QUESTION;
    //     List<Message> messages = new ArrayList<>();
    //     messages.add(Message.systemMessage(systemPrompt));
    //     messages.add(Message.userMessage(userMessage));
    //     return messages;
    // }
}
