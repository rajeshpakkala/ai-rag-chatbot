package com.agent.ai_rag_chatbot.exception;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(

        LocalDateTime timestamp,

        int status,

        String error,

        String message,

        List<String> details

) {
}
