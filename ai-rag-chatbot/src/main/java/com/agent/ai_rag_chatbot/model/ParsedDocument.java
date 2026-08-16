package com.agent.ai_rag_chatbot.model;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ParsedDocument {

    private String documentId;

    private String fileName;

    private String contentType;

    private String content;

    private Integer pageCount;
}
