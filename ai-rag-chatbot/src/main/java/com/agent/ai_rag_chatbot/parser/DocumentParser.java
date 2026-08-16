package com.agent.ai_rag_chatbot.parser;

import com.agent.ai_rag_chatbot.model.ParsedDocument;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentParser {

    boolean supports(String contentType);

    ParsedDocument parse(MultipartFile file);

}
