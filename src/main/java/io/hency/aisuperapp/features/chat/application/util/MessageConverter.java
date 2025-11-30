package io.hency.aisuperapp.features.chat.application.util;

import io.hency.aisuperapp.features.chat.application.domain.entity.Message;
import io.hency.aisuperapp.features.file.application.domain.entity.FileAttachment;
import io.hency.aisuperapp.features.file.application.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageConverter {

    private final FileStorageService fileStorageService;

    public Mono<Map<String, Object>> convertToLlmMessage(Message message, List<FileAttachment> files) {
        if (files == null || files.isEmpty()) {
            // No attachments - simple text message
            return Mono.just(createSimpleTextMessage(message));
        }

        // Has attachments - create content blocks
        return createContentBlocksMessage(message, files);
    }

    private Map<String, Object> createSimpleTextMessage(Message message) {
        Map<String, Object> llmMessage = new HashMap<>();
        llmMessage.put("role", message.getRole().name());
        llmMessage.put("content", message.getContent());
        return llmMessage;
    }

    private Mono<Map<String, Object>> createContentBlocksMessage(Message message, List<FileAttachment> files) {
        List<Mono<Map<String, Object>>> contentBlockMonos = new ArrayList<>();

        // Add text block
        Map<String, Object> textBlock = new HashMap<>();
        textBlock.put("type", "text");
        textBlock.put("text", message.getContent());
        contentBlockMonos.add(Mono.just(textBlock));

        // Add image blocks
        for (FileAttachment file : files) {
            Mono<Map<String, Object>> imageBlockMono = fileStorageService.readFileAsBase64(file.getId())
                .map(base64Data -> {
                    Map<String, Object> source = new HashMap<>();
                    source.put("type", "base64");
                    source.put("media_type", file.getMimeType());
                    source.put("data", base64Data);

                    Map<String, Object> imageBlock = new HashMap<>();
                    imageBlock.put("type", "image");
                    imageBlock.put("source", source);

                    return imageBlock;
                })
                .doOnError(error -> log.error("Failed to read file as base64: {}", file.getId(), error));

            contentBlockMonos.add(imageBlockMono);
        }

        // Combine all content blocks
        return Mono.zip(contentBlockMonos, objects -> {
            List<Map<String, Object>> contentBlocks = new ArrayList<>();
            for (Object obj : objects) {
                contentBlocks.add((Map<String, Object>) obj);
            }

            Map<String, Object> llmMessage = new HashMap<>();
            llmMessage.put("role", message.getRole().name());
            llmMessage.put("content", contentBlocks);

            return llmMessage;
        });
    }
}
