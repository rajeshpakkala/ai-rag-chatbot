package com.agent.ai_rag_chatbot.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RagContext {

    private String question;

    private List<RetrievedChunk> chunks;

    private String formattedContext;
}
