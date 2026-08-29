package com.agent.ai_rag_chatbot.repository;

import com.agent.ai_rag_chatbot.entity.UploadedDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UploadedDocumentRepository
        extends JpaRepository<UploadedDocument, String> {

    boolean existsByContentHash(String contentHash);
}
