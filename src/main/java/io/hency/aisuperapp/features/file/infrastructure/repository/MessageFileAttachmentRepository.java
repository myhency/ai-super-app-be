package io.hency.aisuperapp.features.file.infrastructure.repository;

import io.hency.aisuperapp.features.file.application.domain.entity.MessageFileAttachment;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface MessageFileAttachmentRepository extends R2dbcRepository<MessageFileAttachment, Long> {

    Flux<MessageFileAttachment> findByMessageId(Long messageId);

    @Query("SELECT fa.* FROM file_attachments fa " +
           "INNER JOIN message_file_attachments mfa ON fa.id = mfa.file_attachment_id " +
           "WHERE mfa.message_id = :messageId")
    Flux<io.hency.aisuperapp.features.file.application.domain.entity.FileAttachment> findFileAttachmentsByMessageId(Long messageId);
}
