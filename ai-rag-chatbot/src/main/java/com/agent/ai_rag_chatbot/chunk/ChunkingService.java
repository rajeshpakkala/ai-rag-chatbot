package com.agent.ai_rag_chatbot.chunk;

import com.agent.ai_rag_chatbot.model.*;

import java.util.List;

public interface ChunkingService {

    List<DocumentChunk> chunk(ParsedDocument document);
}
