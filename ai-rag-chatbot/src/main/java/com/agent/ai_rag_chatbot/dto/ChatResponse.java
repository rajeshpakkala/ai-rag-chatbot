package com.agent.ai_rag_chatbot.dto;

import java.util.List;

public record ChatResponse(

        String answer,

        boolean grounded,

        List<CitationDTO> citations

) {

    public static ChatResponse noAnswer(String message) {
        return new ChatResponse(
                message,
                false,
                List.of()
        );
    }
}
