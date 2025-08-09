package io.hency.aisuperapp.features.onedrive.adapter.in;

import io.hency.aisuperapp.features.onedrive.application.domain.entity.OneDriveFile;
import io.hency.aisuperapp.features.onedrive.application.port.in.OneDriveUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/onedrive")
@RequiredArgsConstructor
public class OneDriveController {

    private final OneDriveUseCase oneDriveUseCase;

    @GetMapping("/files")
    public Flux<OneDriveFile> getFiles(@RequestHeader("Authorization") String authorization) {
        String accessToken = extractToken(authorization);
        return oneDriveUseCase.getFiles(accessToken);
    }

    @GetMapping("/files/{fileId}/download")
    public Mono<byte[]> downloadFile(
            @RequestHeader("Authorization") String authorization,
            @PathVariable String fileId) {
        String accessToken = extractToken(authorization);
        return oneDriveUseCase.downloadFile(accessToken, fileId);
    }

    @PostMapping(value = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<OneDriveFile> uploadFile(
            @RequestHeader("Authorization") String authorization,
            @RequestPart("file") FilePart filePart) {
        String accessToken = extractToken(authorization);
        return filePart.content()
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    return bytes;
                })
                .reduce((bytes1, bytes2) -> {
                    byte[] combined = new byte[bytes1.length + bytes2.length];
                    System.arraycopy(bytes1, 0, combined, 0, bytes1.length);
                    System.arraycopy(bytes2, 0, combined, bytes1.length, bytes2.length);
                    return combined;
                })
                .flatMap(content -> oneDriveUseCase.uploadFile(accessToken, filePart.filename(), content));
    }

    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        throw new IllegalArgumentException("Invalid authorization header");
    }
}