package com.agent.ai_rag_chatbot.controller;

import com.agent.ai_rag_chatbot.dto.DocumentUploadResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/chatbot/rag/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final IngestionService ingestionService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<DocumentUploadResponse> upload(
            @RequestParam("files")
            @NotEmpty
            MultipartFile[] files) {

        List<MultipartFile> fileList =
                Arrays.asList(files);

        ingestionService.ingest(fileList);

        List<String> fileNames =
                fileList.stream()
                        .map(MultipartFile::getOriginalFilename)
                        .toList();

        DocumentUploadResponse response =
                new DocumentUploadResponse(
                        "Documents uploaded successfully",
                        fileList.size(),
                        fileNames
                );

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }
}
