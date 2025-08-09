package io.hency.aisuperapp.features.onedrive.application.port.out;

import io.hency.aisuperapp.features.onedrive.application.domain.entity.OneDriveFile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OneDrivePort {
    Flux<OneDriveFile> getFiles(String accessToken);
    Mono<byte[]> downloadFile(String accessToken, String fileId);
    Mono<OneDriveFile> uploadFile(String accessToken, String fileName, byte[] content);
}