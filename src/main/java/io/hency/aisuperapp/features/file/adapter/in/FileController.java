package io.hency.aisuperapp.features.file.adapter.in;

import io.hency.aisuperapp.features.file.adapter.in.dto.FileUploadResponse;
import io.hency.aisuperapp.features.file.application.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<FileUploadResponse> uploadFile(
        @RequestPart("file") FilePart filePart,
        @RequestParam(value = "userId", required = false) Long userId
    ) {
        log.info("Uploading file: {}, userId: {}", filePart.filename(), userId);
        return fileStorageService.saveFile(filePart, userId)
            .map(FileUploadResponse::from)
            .doOnSuccess(response -> log.info("File uploaded successfully: {}", response.getFileId()))
            .doOnError(error -> log.error("Failed to upload file", error));
    }

    @GetMapping("/{fileId}")
    public Mono<ResponseEntity<Resource>> getFile(@PathVariable("fileId") Long fileId) {
        log.info("Getting file: {}", fileId);
        return fileStorageService.getFile(fileId)
            .map(fileAttachment -> {
                Resource resource = new FileSystemResource(Paths.get(fileAttachment.getFilePath()));
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(fileAttachment.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileAttachment.getOriginalName() + "\"")
                    .body(resource);
            })
            .doOnError(error -> log.error("Failed to get file: {}", fileId, error));
    }

    @DeleteMapping("/{fileId}")
    public Mono<Void> deleteFile(@PathVariable("fileId") Long fileId) {
        log.info("Deleting file: {}", fileId);
        return fileStorageService.deleteFile(fileId)
            .doOnSuccess(v -> log.info("File deleted successfully: {}", fileId))
            .doOnError(error -> log.error("Failed to delete file: {}", fileId, error));
    }
}
