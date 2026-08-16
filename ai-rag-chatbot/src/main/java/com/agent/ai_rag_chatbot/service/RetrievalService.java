package com.agent.ai_rag_chatbot.service;

import com.agent.ai_rag_chatbot.model.RetrievedChunk;
import com.agent.ai_rag_chatbot.util.Constants;
import com.agent.ai_rag_chatbot.vector.VectorStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RetrievalService {

    private final VectorStoreService vectorStoreService;

    public List<RetrievedChunk> retrieve(
            String query) {

        return vectorStoreService.search(
                query,
                Constants.Rag.DEFAULT_TOP_K,
                Constants.Rag.DEFAULT_SIMILARITY_THRESHOLD
        );
    }
}
