package com.agent.ai_rag_chatbot.vector;

import com.agent.ai_rag_chatbot.model.DocumentChunk;
import com.agent.ai_rag_chatbot.model.RetrievedChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PgVectorStoreServiceImpl implements VectorStoreService {

    private final VectorStore vectorStore;

    @Override
    public void save(List<DocumentChunk> chunks) {

        List<Document> documents = chunks.stream()
                .map(this::toDocument)
                .toList();

        vectorStore.add(documents);

        log.info("Saved {} chunks to PGVector", documents.size());
    }

    @Override
    public List<RetrievedChunk> search(
            String query,
            int topK,
            double similarityThreshold) {

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .build();

        List<Document> results =
                vectorStore.similaritySearch(request);

        return results.stream()
                .map(this::toRetrievedChunk)
                .toList();
    }

    private Document toDocument(DocumentChunk chunk) {

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("chunkId", chunk.getChunkId());
        metadata.put("documentId", chunk.getDocumentId());
        metadata.put("documentName", chunk.getDocumentName());

        if (chunk.getPageNumber() != null) {
            metadata.put("pageNumber", chunk.getPageNumber());
        }
        if (chunk.getSection() != null) {
            metadata.put("section", chunk.getSection());
        }
        if (chunk.getHeading() != null) {
            metadata.put("heading", chunk.getHeading());
        }
        if (chunk.getChunkIndex() != null) {
            metadata.put("chunkIndex", chunk.getChunkIndex());
        }

        return Document.builder()
                .id(chunk.getChunkId())
                .text(chunk.getContent())
                .metadata(metadata)
                .build();
    }

    private RetrievedChunk toRetrievedChunk(Document doc) {

        Map<String, Object> meta = doc.getMetadata();

        return RetrievedChunk.builder()
                .chunkId(metaString(meta, "chunkId"))
                .documentId(metaString(meta, "documentId"))
                .documentName(metaString(meta, "documentName"))
                .content(doc.getText())
                .pageNumber(metaInt(meta, "pageNumber"))
                .section(metaString(meta, "section"))
                .heading(metaString(meta, "heading"))
                .similarityScore(doc.getScore())
                .build();
    }

    private String metaString(Map<String, Object> meta, String key) {
        Object val = meta.get(key);
        return val != null ? val.toString() : null;
    }

    private Integer metaInt(Map<String, Object> meta, String key) {
        Object val = meta.get(key);
        if (val instanceof Integer i) return i;
        if (val instanceof Number n) return n.intValue();
        return null;
    }
}
