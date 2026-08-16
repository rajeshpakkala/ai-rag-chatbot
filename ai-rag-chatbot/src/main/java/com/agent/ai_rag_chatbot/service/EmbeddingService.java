package com.agent.ai_rag_chatbot.service;

import java.util.List;

public interface EmbeddingService {

    float[] embed(String text);

    List<float[]> embed(List<String> texts);
}
