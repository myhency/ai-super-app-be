package io.hency.aisuperapp.features.onedrive.adapter.out;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hency.aisuperapp.features.onedrive.application.domain.entity.OneDriveFile;
import io.hency.aisuperapp.features.onedrive.application.port.out.OneDrivePort;
import io.hency.aisuperapp.features.onedrive.infrastructure.external.OneDriveApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class OneDriveAdapter implements OneDrivePort {

    private final OneDriveApiClient oneDriveApiClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Flux<OneDriveFile> getFiles(String accessToken) {
        return oneDriveApiClient.getFiles(accessToken)
                .map(this::parseFileResponse)
                .flatMapIterable(jsonNode -> jsonNode.get("value"))
                .map(this::mapToOneDriveFile);
    }

    @Override
    public Mono<byte[]> downloadFile(String accessToken, String fileId) {
        return oneDriveApiClient.getFileContent(accessToken, fileId)
                .map(String::getBytes);
    }

    @Override
    public Mono<OneDriveFile> uploadFile(String accessToken, String fileName, byte[] content) {
        return oneDriveApiClient.uploadFile(accessToken, fileName, content)
                .map(this::parseFileResponse)
                .map(this::mapToOneDriveFile);
    }

    private JsonNode parseFileResponse(String response) {
        try {
            return objectMapper.readTree(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse OneDrive response", e);
        }
    }

    private OneDriveFile mapToOneDriveFile(JsonNode fileNode) {
        return OneDriveFile.builder()
                .id(fileNode.get("id").asText())
                .name(fileNode.get("name").asText())
                .mimeType(fileNode.has("file") ? fileNode.get("file").get("mimeType").asText() : null)
                .size(fileNode.get("size").asLong())
                .downloadUrl(fileNode.has("@microsoft.graph.downloadUrl") ? 
                    fileNode.get("@microsoft.graph.downloadUrl").asText() : null)
                .webUrl(fileNode.get("webUrl").asText())
                .createdDateTime(parseDateTime(fileNode.get("createdDateTime").asText()))
                .lastModifiedDateTime(parseDateTime(fileNode.get("lastModifiedDateTime").asText()))
                .isFolder(fileNode.has("folder"))
                .parentId(fileNode.get("parentReference").get("id").asText())
                .build();
    }

    private LocalDateTime parseDateTime(String dateTimeString) {
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
    }
}