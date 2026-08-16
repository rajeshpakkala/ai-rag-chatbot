package com.agent.ai_rag_chatbot.model;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class DocumentChunk {

    private String chunkId;

    private String documentId;

    private String documentName;

    private String content;

    private Integer pageNumber;

    private String section;

    private String heading;

    private Integer chunkIndex;
}
