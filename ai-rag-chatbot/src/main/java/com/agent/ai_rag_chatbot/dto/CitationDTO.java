package com.agent.ai_rag_chatbot.dto;

public record CitationDTO(

        String documentName,

        Integer pageNumber,

        String chunkId

) {
}
