package com.agent.ai_rag_chatbot.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RetrievedChunk {

    private String chunkId;

    private String documentId;

    private String documentName;

    private String content;

    private Integer pageNumber;

    private String section;

    private String heading;

    private Double similarityScore;
}
