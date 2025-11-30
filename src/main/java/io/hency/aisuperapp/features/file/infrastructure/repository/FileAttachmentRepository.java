package io.hency.aisuperapp.features.file.infrastructure.repository;

import io.hency.aisuperapp.features.file.application.domain.entity.FileAttachment;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface FileAttachmentRepository extends R2dbcRepository<FileAttachment, Long> {

    Flux<FileAttachment> findByUploadedBy(Long uploadedBy);

    Mono<FileAttachment> findById(Long id);
}
