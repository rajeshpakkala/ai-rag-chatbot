package com.agent.ai_rag_chatbot.service;

import com.agent.ai_rag_chatbot.exception.DocumentProcessingException;
import com.agent.ai_rag_chatbot.model.ParsedDocument;
import com.agent.ai_rag_chatbot.parser.DocumentParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentParserService {

    private final List<DocumentParser> documentParsers;

    public ParsedDocument parse(MultipartFile file) {

        String contentType = file.getContentType();

        return documentParsers.stream()
                .filter(parser ->
                        parser.supports(contentType))
                .findFirst()
                .orElseThrow(() ->
                        new DocumentProcessingException(
                                "Unsupported file type: "
                                        + contentType
                        ))
                .parse(file);
    }
}
