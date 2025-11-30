package io.hency.aisuperapp.common.infrastructure.config.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "file.storage")
public class FileStorageProperties {

    private String basePath;
    private List<String> allowedTypes;
    private Long maxSize;
}
