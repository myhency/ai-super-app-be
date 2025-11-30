package io.hency.aisuperapp.features.file.application.service;

import io.hency.aisuperapp.common.infrastructure.config.storage.FileStorageProperties;
import io.hency.aisuperapp.features.file.application.domain.entity.FileAttachment;
import io.hency.aisuperapp.features.file.infrastructure.repository.FileAttachmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageProperties properties;
    private final FileAttachmentRepository fileAttachmentRepository;

    public Mono<FileAttachment> saveFile(FilePart filePart, Long userId) {
        return Mono.fromCallable(() -> {
            // Validate file type
            String contentType = filePart.headers().getContentType() != null
                ? filePart.headers().getContentType().toString()
                : "application/octet-stream";

            if (!properties.getAllowedTypes().contains(contentType)) {
                throw new IllegalArgumentException("File type not allowed: " + contentType);
            }

            // Generate unique file name
            String originalFileName = filePart.filename();
            String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Create storage directory if not exists
            Path storagePath = Paths.get(properties.getBasePath());
            if (!Files.exists(storagePath)) {
                Files.createDirectories(storagePath);
            }

            // Full file path
            Path filePath = storagePath.resolve(uniqueFileName);

            return new FileInfo(filePath, uniqueFileName, originalFileName, contentType);
        })
        .flatMap(fileInfo ->
            // Save file to disk
            DataBufferUtils.write(filePart.content(), fileInfo.filePath)
                .then(Mono.fromCallable(() -> {
                    long fileSize = Files.size(fileInfo.filePath);

                    // Validate file size
                    if (fileSize > properties.getMaxSize()) {
                        Files.deleteIfExists(fileInfo.filePath);
                        throw new IllegalArgumentException("File size exceeds maximum allowed: " + properties.getMaxSize());
                    }

                    return fileSize;
                }))
                .flatMap(fileSize -> {
                    // Save metadata to database
                    FileAttachment fileAttachment = FileAttachment.builder()
                        .fileName(fileInfo.uniqueFileName)
                        .originalName(fileInfo.originalFileName)
                        .filePath(fileInfo.filePath.toString())
                        .fileSize(fileSize)
                        .mimeType(fileInfo.contentType)
                        .uploadedBy(userId)
                        .createdAt(LocalDateTime.now())
                        .build();

                    return fileAttachmentRepository.save(fileAttachment);
                })
        )
        .doOnSuccess(file -> log.info("File saved successfully: {}", file.getFileName()))
        .doOnError(error -> log.error("Failed to save file", error));
    }

    public Mono<FileAttachment> getFile(Long fileId) {
        return fileAttachmentRepository.findById(fileId)
            .switchIfEmpty(Mono.error(new RuntimeException("File not found: " + fileId)));
    }

    public Mono<Void> deleteFile(Long fileId) {
        return fileAttachmentRepository.findById(fileId)
            .flatMap(fileAttachment ->
                Mono.fromCallable(() -> {
                    Path filePath = Paths.get(fileAttachment.getFilePath());
                    Files.deleteIfExists(filePath);
                    return fileAttachment;
                })
                .flatMap(file -> fileAttachmentRepository.delete(file))
            )
            .doOnSuccess(v -> log.info("File deleted successfully: {}", fileId))
            .doOnError(error -> log.error("Failed to delete file: {}", fileId, error));
    }

    public Mono<String> readFileAsBase64(Long fileId) {
        return fileAttachmentRepository.findById(fileId)
            .flatMap(fileAttachment ->
                Mono.fromCallable(() -> {
                    Path filePath = Paths.get(fileAttachment.getFilePath());

                    if (!Files.exists(filePath)) {
                        throw new IOException("File not found on disk: " + filePath);
                    }

                    byte[] fileBytes = Files.readAllBytes(filePath);
                    return Base64.getEncoder().encodeToString(fileBytes);
                })
            )
            .doOnError(error -> log.error("Failed to read file as base64: {}", fileId, error));
    }

    private static class FileInfo {
        private final Path filePath;
        private final String uniqueFileName;
        private final String originalFileName;
        private final String contentType;

        public FileInfo(Path filePath, String uniqueFileName, String originalFileName, String contentType) {
            this.filePath = filePath;
            this.uniqueFileName = uniqueFileName;
            this.originalFileName = originalFileName;
            this.contentType = contentType;
        }
    }
}
