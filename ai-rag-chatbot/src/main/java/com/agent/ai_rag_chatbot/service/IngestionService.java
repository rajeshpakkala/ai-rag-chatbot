package com.agent.ai_rag_chatbot.service;

import com.agent.ai_rag_chatbot.chunk.ChunkingService;
import com.agent.ai_rag_chatbot.model.*;
import com.agent.ai_rag_chatbot.vector.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final DocumentParserService documentParserService;

    private final ChunkingService chunkingService;

    private final VectorStoreService vectorStoreService;


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


        log.info(
                "Document processed successfully: {}",
                fileName
        );
    }
}
