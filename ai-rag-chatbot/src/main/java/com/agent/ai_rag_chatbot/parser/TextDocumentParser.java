package com.agent.ai_rag_chatbot.parser;

import com.agent.ai_rag_chatbot.exception.DocumentProcessingException;
import com.agent.ai_rag_chatbot.model.ParsedDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class TextDocumentParser implements DocumentParser {

    @Override
    public boolean supports(String contentType) {

        return "text/plain".equalsIgnoreCase(contentType)
                || "text/markdown".equalsIgnoreCase(contentType);
    }

    @Override
    public ParsedDocument parse(MultipartFile file) {

        try {

            String text = new String(
                    file.getBytes(),
                    StandardCharsets.UTF_8
            );

            return ParsedDocument.builder()
                    .documentId(UUID.randomUUID().toString())
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .content(text)
                    .build();

        } catch (Exception exception) {

            log.error(
                    "Failed to parse text file: {}",
                    file.getOriginalFilename(),
                    exception
            );

            throw new DocumentProcessingException(
                    "Unable to parse text file",
                    exception
            );
        }
    }
}
