package io.hency.aisuperapp.features.topic.domain.service;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.constant.SystemPrompt;
import io.hency.aisuperapp.common.error.ErrorCode;
import io.hency.aisuperapp.common.error.exception.TopicNotFoundException;
import io.hency.aisuperapp.features.chat.application.port.out.ChatPort;
import io.hency.aisuperapp.features.chat.application.domain.entity.Message;
import io.hency.aisuperapp.features.topic.application.port.in.TopicUseCase;
import io.hency.aisuperapp.features.topic.application.port.out.TopicPort;
import io.hency.aisuperapp.features.topic.domain.entity.Topic;
import io.hency.aisuperapp.features.topic.domain.entity.TopicEntity;
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
@RequiredArgsConstructor
public class TopicService implements TopicUseCase {
    private final TopicPort topicPort;
    private final ChatPort chatPort;

    @Override
    public Mono<Topic> generate(Ulid topicId, Ulid userId) {
        Mono<TopicEntity> topicEntityMono = topicPort.findTopicByTopicIdAndUserId(topicId, userId)
                .switchIfEmpty(Mono.error(new TopicNotFoundException(ErrorCode.H400A)))
                .cache();

        Flux<Message> summarizeTopicFlux = topicEntityMono
                .flatMapMany(topicEntity -> chatPort.findFirstChatByTopicId(topicEntity.getUlid())
                        .flatMapMany(chat -> {
                            List<Message> messages = this.createMessages(chat.message().content());
                            return chatPort.sendChat(messages);
                        }));

        var topic = summarizeTopicFlux
                .collectList()
                .map(messages -> messages.stream()
                        .map(Message::content)
                        .collect(Collectors.joining()));

        return topic
                .zipWith(topicEntityMono)
                .flatMap(tuple -> {
                    var topicEntity = tuple.getT2();
                    String summarizedContent = tuple.getT1();
                    topicEntity.updateTitle(summarizedContent);
                    return topicPort.save(topicEntity);
                })
                .map(Topic::of);
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

    private List<Message> createMessages(String userMessage) {
        String systemPrompt = SystemPrompt.SUMMARIZE_USER_QUESTION;
        List<Message> messages = new ArrayList<>();
        messages.add(Message.systemMessage(systemPrompt));
        messages.add(Message.userMessage(userMessage));

        return messages;
    }
}
