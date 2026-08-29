package com.agent.ai_rag_chatbot.service;

import com.agent.ai_rag_chatbot.chunk.ChunkingService;
import com.agent.ai_rag_chatbot.entity.UploadedDocument;
import com.agent.ai_rag_chatbot.exception.DocumentAlreadyExistsException;
import com.agent.ai_rag_chatbot.exception.DocumentProcessingException;
import com.agent.ai_rag_chatbot.model.*;
import com.agent.ai_rag_chatbot.repository.UploadedDocumentRepository;
import com.agent.ai_rag_chatbot.vector.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final DocumentParserService documentParserService;

    private final ChunkingService chunkingService;

    private final VectorStoreService vectorStoreService;

    private final UploadedDocumentRepository uploadedDocumentRepository;


    public void ingest(List<MultipartFile> files) {

        if (files == null || files.isEmpty()) {

            throw new IllegalArgumentException(
                    "At least one document is required"
            );
        }

        log.info(
                "Starting document ingestion. Total files: {}",
                files.size()
        );

        for (MultipartFile file : files) {

            processDocument(file);
        }

        log.info(
                "Document ingestion completed. Total files: {}",
                files.size()
        );
    }


    private void processDocument(
            MultipartFile file) {

        String fileName =
                file.getOriginalFilename();

        log.info(
                "Processing document: {}",
                fileName
        );

        /*
         * STEP 0
         * Reject duplicate content before doing
         * any parsing/embedding work.
         */
        String contentHash = sha256Hex(file);

        if (uploadedDocumentRepository.existsByContentHash(contentHash)) {

            throw new DocumentAlreadyExistsException(
                    "Document already present: " + fileName
            );
        }

        /*
         * STEP 1
         * Parse document
         */
        ParsedDocument parsedDocument =
                documentParserService.parse(file);


        /*
         * STEP 2
         * Chunk document
         */
        List<DocumentChunk> chunks =
                chunkingService.chunk(parsedDocument);


        if (chunks.isEmpty()) {

            log.warn(
                    "No chunks generated for document: {}",
                    fileName
            );

            return;
        }


        log.info(
                "Generated {} chunks for document: {}",
                chunks.size(),
                fileName
        );


        /*
         * STEP 3
         * Store chunks
         *
         * Currently this will be handled by
         * VectorStoreService.
         *
         * Later this becomes PGVector.
         */
        vectorStoreService.save(chunks);


        /*
         * STEP 4
         * Record the document as uploaded so
         * future duplicate uploads are rejected.
         */
        uploadedDocumentRepository.save(
                UploadedDocument.builder()
                        .documentId(parsedDocument.getDocumentId())
                        .fileName(fileName)
                        .contentHash(contentHash)
                        .uploadedAt(LocalDateTime.now())
                        .build()
        );


        log.info(
                "Document processed successfully: {}",
                fileName
        );
    }

    private String sha256Hex(MultipartFile file) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(file.getBytes());

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException | IOException exception) {

            throw new DocumentProcessingException(
                    "Unable to read document content",
                    exception
            );
        }
    }
}
