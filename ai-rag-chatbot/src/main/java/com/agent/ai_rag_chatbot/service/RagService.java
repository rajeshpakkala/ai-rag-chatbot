package com.agent.ai_rag_chatbot.service;

import com.agent.ai_rag_chatbot.dto.*;
import com.agent.ai_rag_chatbot.model.*;
import com.agent.ai_rag_chatbot.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final RetrievalService retrievalService;

    private final GroundedPromptService
            groundedPromptService;

    private final ChatClient chatClient;

    public ChatResponse answer(String question) {

        log.info(
                "RAG request received for question: {}",
                question
        );

        /*
         * STEP 1
         * Retrieve relevant document chunks.
         */
        List<RetrievedChunk> retrievedChunks =
                retrievalService.retrieve(question);

        /*
         * STEP 2
         * Grounding gate.
         *
         * If nothing relevant was found,
         * DO NOT call the LLM.
         */
        if (retrievedChunks == null
                || retrievedChunks.isEmpty()) {

            log.info(
                    "No relevant documents found for question: {}",
                    question
            );

            return ChatResponse.noAnswer(
                    Constants.Messages
                            .NO_RELEVANT_INFORMATION
            );
        }

        /*
         * STEP 3
         * Limit context size.
         */
        List<RetrievedChunk> contextChunks =
                retrievedChunks.stream()
                        .limit(
                                Constants.Rag
                                        .MAX_CONTEXT_CHUNKS
                        )
                        .toList();

        /*
         * STEP 4
         * Build context.
         */
        String formattedContext =
                buildContext(contextChunks);

        RagContext ragContext =
                RagContext.builder()
                        .question(question)
                        .chunks(contextChunks)
                        .formattedContext(formattedContext)
                        .build();

        /*
         * STEP 5
         * Build grounded prompt.
         */
        String prompt =
                groundedPromptService
                        .buildPrompt(ragContext);

        /*
         * STEP 6
         * Call LLM.
         */
        String answer;

        try {

            answer = chatClient
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

        } catch (Exception exception) {

            log.error(
                    "LLM call failed",
                    exception
            );

            throw new RuntimeException(
                    "Failed to generate AI response",
                    exception
            );
        }

        /*
         * STEP 7
         * Build citations.
         */
        var citations =
                contextChunks.stream()
                        .map(chunk ->
                                new CitationDTO(
                                        chunk.getDocumentName(),
                                        chunk.getPageNumber(),
                                        chunk.getChunkId()
                                )
                        )
                        .toList();

        /*
         * STEP 8
         * Return grounded response.
         */
        return new ChatResponse(
                answer,
                true,
                citations
        );
    }

    private String buildContext(
            List<RetrievedChunk> chunks) {

        StringBuilder context =
                new StringBuilder();

        for (RetrievedChunk chunk : chunks) {

            context.append("\n--- SOURCE ---\n");

            context.append("Document: ")
                    .append(chunk.getDocumentName())
                    .append("\n");

            context.append("Page: ")
                    .append(chunk.getPageNumber())
                    .append("\n");

            context.append("Chunk ID: ")
                    .append(chunk.getChunkId())
                    .append("\n");

            context.append("Similarity Score: ")
                    .append(chunk.getSimilarityScore())
                    .append("\n");

            context.append("Content:\n")
                    .append(chunk.getContent())
                    .append("\n");
        }

        return context.toString();
    }
}
