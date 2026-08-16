package com.agent.ai_rag_chatbot.controller;

import com.agent.ai_rag_chatbot.dto.*;
import com.agent.ai_rag_chatbot.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chatbot/rag/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @Valid @RequestBody ChatRequest request) {

        ChatResponse response =
                chatService.chat(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }
}
