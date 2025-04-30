package io.hency.aisuperapp.features.chat.adapter.in;

import com.github.f4b6a3.ulid.Ulid;
import io.hency.aisuperapp.features.chat.adapter.in.dto.ChatRequest;
import io.hency.aisuperapp.features.chat.application.port.in.ChatUseCase;
import io.hency.aisuperapp.features.chat.application.domain.entity.Chat;
import io.hency.aisuperapp.features.topic.application.port.in.TopicUseCase;
import io.hency.aisuperapp.features.topic.application.domain.entity.Topic;
import io.hency.aisuperapp.features.user.domain.entity.User;
import io.hency.aisuperapp.infrastructure.config.web.context.UserContextHolder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Set;
import java.util.stream.Collectors;


@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/v1/chat")
public class ChatController {
    private final TopicUseCase topicUseCase;
    private final ChatUseCase chatUseCase;

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Chat> sendMessage(
            @Valid @RequestBody Mono<ChatRequest.SendMessageRequest> request
    ) {
        return request
                .doOnNext(this::validate)
                .zipWith(UserContextHolder.getUserMono())
                .flatMapMany(this::createTopic)
                .flatMap(this::sendMessage);
    }

    @PostMapping(value = "/{userChatId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Chat> reSendMessage(
            @PathVariable String userChatId,
            @Valid @RequestBody Mono<ChatRequest.ReSendMessageRequest> _request
    ) {
        _request
                .doOnNext(this::validate)
                .zipWith(UserContextHolder.getUserMono())
                .flatMapMany(tuple -> {
                    var user = tuple.getT2();
                    var request = tuple.getT1();
                    return null;
                });
        return null;
    }


    private <T> void validate(T request) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    violations.stream()
                            .map(ConstraintViolation::getMessage)
                            .collect(Collectors.joining(", ")));
        }
    }

    private Flux<TopicWithContext> createTopic(Tuple2<ChatRequest.SendMessageRequest, User> tuple) {
        var request = tuple.getT1();
        var user = tuple.getT2();
        var userUlid = Ulid.from(user.id());

        return topicUseCase.createDefaultTopic(request.topicUlid(), userUlid)
                .map(topic -> new TopicWithContext(topic, request, userUlid, user.tenantId()))
                .flux();
    }

    private Flux<Chat> sendMessage(TopicWithContext context) {
        var command = context.request().toCommand(
                context.topic().id(),
                context.userUlid(),
                context.tenantId()
        );
        return chatUseCase.send(command, null);
    }

    private record TopicWithContext(
            Topic topic,
            ChatRequest.SendMessageRequest request,
            Ulid userUlid,
            String tenantId
    ) {}
}
