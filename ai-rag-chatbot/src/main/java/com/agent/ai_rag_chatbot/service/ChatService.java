package com.agent.ai_rag_chatbot.service;

import com.agent.ai_rag_chatbot.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final RagService ragService;

    public ChatResponse chat(ChatRequest request) {

        return ragService.answer(
                request.message()
        );
    }
}
