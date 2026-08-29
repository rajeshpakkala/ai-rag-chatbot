package com.agent.ai_rag_chatbot.service;

import com.agent.ai_rag_chatbot.model.RetrievedChunk;
import com.agent.ai_rag_chatbot.util.Constants;
import com.agent.ai_rag_chatbot.vector.VectorStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetrievalService {

    private final VectorStoreService vectorStoreService;

    public List<RetrievedChunk> retrieve(
            String query) {

        List<RetrievedChunk> chunks = vectorStoreService.search(
                query,
                Constants.Rag.DEFAULT_TOP_K,
                Constants.Rag.DEFAULT_SIMILARITY_THRESHOLD
        );

        log.info(
                "Retrieved {} chunks for query '{}' (threshold={}). Top score: {}",
                chunks.size(),
                query,
                Constants.Rag.DEFAULT_SIMILARITY_THRESHOLD,
                chunks.isEmpty() ? "n/a" : chunks.get(0).getSimilarityScore()
        );

        return chunks;
    }
}
