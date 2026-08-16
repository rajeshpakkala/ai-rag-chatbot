package com.agent.ai_rag_chatbot.vector;


import com.agent.ai_rag_chatbot.model.*;

import java.util.List;

public interface VectorStoreService {

    void save(List<DocumentChunk> chunks);

    List<RetrievedChunk> search(
            String query,
            int topK,
            double similarityThreshold
    );
}
