package com.agent.ai_rag_chatbot.dto;


import jakarta.validation.constraints.NotBlank;

public record ChatRequest(

        @NotBlank(message = "Message is required")
        String message

) {
}
