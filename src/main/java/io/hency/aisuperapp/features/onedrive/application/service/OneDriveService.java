package io.hency.aisuperapp.features.onedrive.application.service;

import io.hency.aisuperapp.features.onedrive.application.domain.entity.OneDriveFile;
import io.hency.aisuperapp.features.onedrive.application.port.in.OneDriveUseCase;
import io.hency.aisuperapp.features.onedrive.application.port.out.OneDrivePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class OneDriveService implements OneDriveUseCase {

    private final OneDrivePort oneDrivePort;

    @Override
    public Flux<OneDriveFile> getFiles(String accessToken) {
        return oneDrivePort.getFiles(accessToken);
    }

    @Override
    public Mono<byte[]> downloadFile(String accessToken, String fileId) {
        return oneDrivePort.downloadFile(accessToken, fileId);
    }

    @Override
    public Mono<OneDriveFile> uploadFile(String accessToken, String fileName, byte[] content) {
        return oneDrivePort.uploadFile(accessToken, fileName, content);
    }
}