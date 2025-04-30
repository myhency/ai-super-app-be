package io.hency.aisuperapp.features.topic.adapter.in;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.common.util.UlidUtils;
import io.hency.aisuperapp.features.topic.adapter.in.dto.GenerateTopicResponse;
import io.hency.aisuperapp.features.topic.application.port.in.TopicUseCase;
import io.hency.aisuperapp.common.infrastructure.config.web.context.UserContextHolder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/topic")
public class TopicController {
    private final TopicUseCase topicUseCase;

    @PostMapping("/{topicId}/summary")
    public Mono<GenerateTopicResponse> generateTopic(@PathVariable String topicId) {
        if(!Ulid.isValid(topicId)) {
            throw new IllegalArgumentException("Invalid topicId");
        }

        return UserContextHolder.getUserMono()
                .flatMap(user -> topicUseCase.generate(UlidUtils.of(topicId), UlidUtils.of(user.id())))
                .map(GenerateTopicResponse::of);
    }
}
