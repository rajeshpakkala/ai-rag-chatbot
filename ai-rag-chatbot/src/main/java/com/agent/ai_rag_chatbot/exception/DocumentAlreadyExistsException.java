package com.agent.ai_rag_chatbot.exception;


public class DocumentAlreadyExistsException
        extends RuntimeException {

    public DocumentAlreadyExistsException(String message) {
        super(message);
    }
}
