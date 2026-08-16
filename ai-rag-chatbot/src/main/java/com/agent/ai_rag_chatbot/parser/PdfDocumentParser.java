package com.agent.ai_rag_chatbot.parser;

import com.agent.ai_rag_chatbot.exception.DocumentProcessingException;
import com.agent.ai_rag_chatbot.model.ParsedDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Component
public class PdfDocumentParser implements DocumentParser {

    private final Tika tika = new Tika();

    @Override
    public boolean supports(String contentType) {

        return "application/pdf".equalsIgnoreCase(contentType);
    }

    @Override
    public ParsedDocument parse(MultipartFile file) {

        try (InputStream inputStream = file.getInputStream()) {

            String text = tika.parseToString(inputStream);

            return ParsedDocument.builder()
                    .documentId(UUID.randomUUID().toString())
                    .fileName(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .content(text)
                    .build();

        } catch (Exception exception) {

            log.error(
                    "Failed to parse PDF: {}",
                    file.getOriginalFilename(),
                    exception
            );

            throw new DocumentProcessingException(
                    "Unable to parse PDF: "
                            + file.getOriginalFilename(),
                    exception
            );
        }
    }
}
