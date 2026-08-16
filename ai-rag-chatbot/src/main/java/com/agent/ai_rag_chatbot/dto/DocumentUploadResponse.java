package com.agent.ai_rag_chatbot.dto;

import java.util.List;

public record DocumentUploadResponse(

        String message,

        int totalFiles,

        List<String> fileNames

) {
}
