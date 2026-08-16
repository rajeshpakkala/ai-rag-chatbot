package com.agent.ai_rag_chatbot.service;

import com.agent.ai_rag_chatbot.model.RagContext;
import org.springframework.stereotype.Service;

@Service
public class GroundedPromptService {

    public String buildPrompt(
            RagContext ragContext) {

        return """
                You are a document-grounded AI assistant.

                STRICT RULES:

                1. Answer ONLY using the supplied CONTEXT.
                2. Do not use your general knowledge.
                3. Do not invent facts.
                4. Do not make assumptions that are not supported
                   by the CONTEXT.
                5. If the answer cannot be determined from the
                   CONTEXT, say:
                   
                   "I couldn't find this information in the
                   uploaded documents."

                6. Every factual statement must be supported
                   by the supplied context.

                CONTEXT:
                %s

                USER QUESTION:
                %s
                """
                .formatted(
                        ragContext.getFormattedContext(),
                        ragContext.getQuestion()
                );
    }
}
