package com.agent.ai_rag_chatbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAiEmbeddingService
        implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    @Override
    public float[] embed(String text) {

        return embeddingModel.embed(text);
    }

    @Override
    public List<float[]> embed(List<String> texts) {

        return texts.stream()
                .map(embeddingModel::embed)
                .toList();
    }
}
