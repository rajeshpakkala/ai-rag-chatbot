package com.agent.ai_rag_chatbot.chunk;

import com.agent.ai_rag_chatbot.model.*;
import com.agent.ai_rag_chatbot.util.Constants;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BasicChunkingService
        implements ChunkingService {

    @Override
    public List<DocumentChunk> chunk(
            ParsedDocument document) {

        String content = document.getContent();

        if (content == null || content.isBlank()) {
            return List.of();
        }

        int chunkSize =
                Constants.Rag.DEFAULT_CHUNK_SIZE;

        int overlap =
                Constants.Rag.DEFAULT_CHUNK_OVERLAP;

        List<DocumentChunk> chunks =
                new ArrayList<>();

        int start = 0;
        int chunkIndex = 0;

        while (start < content.length()) {

            int end = Math.min(
                    start + chunkSize,
                    content.length()
            );

            String chunkText =
                    content.substring(start, end).trim();

            if (!chunkText.isBlank()) {

                DocumentChunk chunk =
                        DocumentChunk.builder()
                                .chunkId(
                                        document.getDocumentId()
                                                + "-chunk-"
                                                + chunkIndex
                                )
                                .documentId(
                                        document.getDocumentId()
                                )
                                .documentName(
                                        document.getFileName()
                                )
                                .content(chunkText)
                                .chunkIndex(chunkIndex)
                                .build();

                chunks.add(chunk);
            }

            if (end == content.length()) {
                break;
            }

            start = end - overlap;
            chunkIndex++;
        }

        return chunks;
    }
}
