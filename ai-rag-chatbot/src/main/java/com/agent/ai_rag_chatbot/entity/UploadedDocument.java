package com.agent.ai_rag_chatbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "uploaded_documents",
        uniqueConstraints = @UniqueConstraint(columnNames = "content_hash")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedDocument {

    @Id
    private String documentId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_hash", nullable = false, unique = true)
    private String contentHash;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
}
