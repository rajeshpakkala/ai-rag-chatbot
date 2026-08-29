package com.agent.ai_rag_chatbot.util;

public final class Constants {

    private Constants() {
    }

    public static final class Rag {

        public static final double DEFAULT_SIMILARITY_THRESHOLD = 0.5;

        private Rag() {
        }

        public static final int DEFAULT_TOP_K = 10;

        public static final int MAX_CONTEXT_CHUNKS = 5;

        public static final int DEFAULT_CHUNK_SIZE = 1000;

        public static final int DEFAULT_CHUNK_OVERLAP = 150;
    }

    public static final class Messages {

        private Messages() {
        }

        public static final String
                NO_RELEVANT_INFORMATION =
                "I couldn't find this information in the uploaded documents.";

        public static final String
                INTERNAL_SERVER_ERROR =
                "An unexpected error occurred. Please try again.";

        public static final String
                DOCUMENT_PROCESSING_FAILED =
                "Failed to process the document.";

        public static final String
                INVALID_REQUEST =
                "Invalid request.";
    }
}
