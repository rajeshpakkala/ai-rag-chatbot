# AI RAG Chatbot — Working Procedure

## Table of Contents

1. [What This Application Does](#1-what-this-application-does)
2. [Technology Stack](#2-technology-stack)
3. [System Architecture Overview](#3-system-architecture-overview)
4. [How to Run the Application](#4-how-to-run-the-application)
5. [Flow 1 — Document Ingestion](#5-flow-1--document-ingestion)
6. [Flow 2 — Chat / Question Answering](#6-flow-2--chat--question-answering)
7. [API Reference](#7-api-reference)
8. [Configuration Reference](#8-configuration-reference)
9. [Error Handling](#9-error-handling)
10. [Package Structure](#10-package-structure)

---

## 1. What This Application Does

This is a **Retrieval-Augmented Generation (RAG) chatbot backend**. RAG is an AI pattern that prevents the LLM from hallucinating by forcing it to answer questions *only* using content that you have uploaded — not its general training knowledge.

The application works in two distinct phases:

- **Ingestion Phase** — You upload documents (PDF or plain text). The system reads them, breaks them into chunks, converts those chunks into vector embeddings, and stores them in a PostgreSQL vector database (PGVector).
- **Chat Phase** — You ask a question. The system converts your question into a vector, finds the most similar document chunks in the database, and passes only those chunks to the LLM as context. The LLM is instructed to answer strictly from that context and nothing else.

This approach ensures all answers are grounded in the documents you uploaded, and every response includes citations pointing back to the exact source document, page, and chunk.

---

## 2. Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| AI Orchestration | Spring AI 2.0.0 |
| LLM (Chat) | OpenAI `gpt-4o-mini` |
| Embeddings | OpenAI `text-embedding-3-small` (1536 dimensions) |
| Vector Database | PostgreSQL 17 with PGVector extension |
| Vector Index | HNSW (Hierarchical Navigable Small World) |
| Distance Metric | Cosine Distance |
| Document Parsing | Apache Tika (PDF), native Java (plain text / markdown) |
| Database ORM | Spring Data JPA / Hibernate |
| Build Tool | Maven (Maven Wrapper) |
| Containerization | Docker (PGVector official image) |

---

## 3. System Architecture Overview

```
                        ┌─────────────────────────────────────────┐
                        │           Spring Boot Application        │
                        │                                          │
  ┌──────────┐  upload  │  DocumentController                      │
  │  Client  │ ───────► │       └─► IngestionService               │
  │ (Postman │          │               ├─► DocumentParserService  │
  │ / UI)    │          │               │       ├─► PdfParser      │
  │          │          │               │       └─► TextParser     │
  │          │          │               ├─► BasicChunkingService   │
  │          │          │               └─► PgVectorStoreServiceImpl│
  │          │          │                       └─► VectorStore ──────► PGVector DB
  │          │          │                                          │
  │          │  chat    │  ChatController                          │
  │          │ ───────► │       └─► ChatService                    │
  │          │          │               └─► RagService             │
  │          │          │                   ├─► RetrievalService   │
  │          │          │                   │   └─► PgVectorStoreServiceImpl
  │          │          │                   │       └─► VectorStore ──► PGVector DB
  │          │◄──────── │                   ├─► GroundedPromptService│
  │          │ response │                   └─► ChatClient ────────────► OpenAI API
  └──────────┘          └─────────────────────────────────────────┘
```

---

## 4. How to Run the Application

### Prerequisites

- Java 21 installed and `JAVA_HOME` pointing to it
- Docker Desktop running
- An OpenAI API key

### Step 1 — Start the PGVector Database

From inside the `ai-rag-chatbot` directory:

```bash
docker-compose up -d
```

This spins up a PostgreSQL 17 container with the PGVector extension pre-installed. It creates:
- Database: `ragdb`
- Username: `raguser`
- Password: `ragpassword`
- Port: `5432`

Data is persisted in a Docker volume named `rag_postgres_data` so it survives container restarts.

### Step 2 — Set the OpenAI API Key

PowerShell:
```powershell
$env:OPENAI_API_KEY = "sk-..."
```

Or add it to your system environment variables permanently.

### Step 3 — Start the Application

```bash
./mvnw spring-boot:run
```

On first startup, Spring AI will automatically create the `vector_store` table in PGVector with the HNSW index. You do not need to run any SQL manually.

The application starts on **http://localhost:8080**.

---

## 5. Flow 1 — Document Ingestion

This is the pipeline that runs when you upload a document.

### Endpoint
```
POST /chatbot/rag/api/documents/upload
Content-Type: multipart/form-data
```

### Step-by-step Pipeline

```
MultipartFile(s)
     │
     ▼
[1] DocumentController.upload()
     │  Accepts one or more files as multipart form data.
     │  Converts the array to a list and calls IngestionService.
     │
     ▼
[2] IngestionService.ingest()
     │  Iterates over each file and calls processDocument() for each one.
     │
     ▼
[3] DocumentParserService.parse()
     │  Looks at the file's Content-Type header to decide which parser to use.
     │  - "application/pdf"          → PdfDocumentParser
     │  - "text/plain"               → TextDocumentParser
     │  - "text/markdown"            → TextDocumentParser
     │  If no parser supports the type, throws DocumentProcessingException.
     │
     ▼
[4] Parser (Pdf or Text)
     │  PdfDocumentParser uses Apache Tika to extract all text from the PDF,
     │  including text from embedded fonts and structured content.
     │
     │  TextDocumentParser reads the raw bytes as UTF-8 text.
     │
     │  Both parsers produce a ParsedDocument object containing:
     │    - documentId   : a fresh UUID
     │    - fileName     : original file name
     │    - contentType  : MIME type
     │    - content      : full extracted text as a single string
     │
     ▼
[5] BasicChunkingService.chunk()
     │  Splits the full text into overlapping chunks.
     │
     │  Parameters (from Constants.Rag):
     │    - Chunk size    : 1000 characters
     │    - Overlap       : 150 characters
     │
     │  How it works:
     │    start = 0
     │    Loop:
     │      end = min(start + 1000, text.length)
     │      chunkText = text[start..end]
     │      next start = end - 150   ← overlap ensures no sentence is lost at a boundary
     │
     │  Each chunk gets a DocumentChunk object with:
     │    - chunkId      : "{documentId}-chunk-{index}"
     │    - documentId   : from ParsedDocument
     │    - documentName : original file name
     │    - content      : the 1000-character text slice
     │    - chunkIndex   : sequential index (0, 1, 2, ...)
     │
     ▼
[6] PgVectorStoreServiceImpl.save()
     │  Converts each DocumentChunk into a Spring AI Document object.
     │  The DocumentChunk's fields are stored in the Document's metadata map
     │  so they can be retrieved later during search:
     │    metadata = {
     │      "chunkId"      : "abc-chunk-0",
     │      "documentId"   : "abc",
     │      "documentName" : "report.pdf",
     │      "pageNumber"   : null (not populated by current parsers),
     │      "section"      : null,
     │      "heading"      : null,
     │      "chunkIndex"   : 0
     │    }
     │
     │  Calls vectorStore.add(documents).
     │
     ▼
[7] Spring AI VectorStore (PgVectorStore)
     │  For each Document:
     │    1. Calls OpenAI text-embedding-3-small API to get a 1536-dim embedding vector.
     │    2. Stores the vector + metadata in the `vector_store` table in PostgreSQL.
     │    3. The HNSW index is updated automatically.
     │
     ▼
[8] DocumentController returns DocumentUploadResponse
     HTTP 202 ACCEPTED
     {
       "message": "Documents uploaded successfully",
       "count": 1,
       "fileNames": ["report.pdf"]
     }
```

---

## 6. Flow 2 — Chat / Question Answering

This is the pipeline that runs when you ask a question.

### Endpoint
```
POST /chatbot/rag/api/chat
Content-Type: application/json
```

### Step-by-step Pipeline

```
{ "message": "What is the refund policy?" }
     │
     ▼
[1] ChatController.chat()
     │  Validates the request body (@NotBlank on message field).
     │  Delegates to ChatService.
     │
     ▼
[2] ChatService.chat()
     │  Thin orchestration layer. Extracts the message string
     │  from the ChatRequest record and calls RagService.answer().
     │
     ▼
[3] RagService.answer() — The core RAG logic
     │
     ├─[3a] RetrievalService.retrieve(question)
     │         │
     │         ▼
     │       PgVectorStoreServiceImpl.search(query, topK=10, threshold=0.75)
     │         │
     │         ▼
     │       Spring AI VectorStore.similaritySearch(SearchRequest)
     │         1. Calls OpenAI text-embedding-3-small to embed the question
     │            into a 1536-dimensional vector.
     │         2. Runs a cosine similarity search via HNSW index in PGVector.
     │         3. Returns up to 10 Documents whose similarity score >= 0.75.
     │         4. Maps each Spring AI Document back to a RetrievedChunk,
     │            restoring chunkId, documentId, documentName, content,
     │            pageNumber, section, heading, and similarityScore.
     │
     ├─[3b] Grounding Gate
     │         If no chunks came back (empty list), the LLM is NOT called.
     │         Returns immediately:
     │         {
     │           "answer": "I couldn't find this information in the uploaded documents.",
     │           "grounded": false,
     │           "citations": []
     │         }
     │         This prevents hallucination and saves OpenAI API cost.
     │
     ├─[3c] Context Limiting
     │         Even if 10 chunks were retrieved, only the top 5 are used
     │         (MAX_CONTEXT_CHUNKS = 5). This keeps the LLM prompt within
     │         token limits and focuses it on the most relevant content.
     │
     ├─[3d] Context Formatting (buildContext)
     │         The 5 chunks are formatted into a structured string block:
     │
     │         --- SOURCE ---
     │         Document: report.pdf
     │         Page: 2
     │         Chunk ID: abc-chunk-3
     │         Similarity Score: 0.91
     │         Content:
     │         <the actual 1000-character text chunk>
     │
     │         --- SOURCE ---
     │         Document: policy.txt
     │         ...
     │
     ├─[3e] Grounded Prompt Building (GroundedPromptService)
     │         Wraps the context + user question into a strict system prompt:
     │
     │         "You are a document-grounded AI assistant.
     │          STRICT RULES:
     │          1. Answer ONLY using the supplied CONTEXT.
     │          2. Do not use your general knowledge.
     │          3. Do not invent facts.
     │          ...
     │          CONTEXT:
     │          [formatted chunks]
     │          USER QUESTION:
     │          [original question]"
     │
     ├─[3f] LLM Call (ChatClient → OpenAI gpt-4o-mini)
     │         Sends the full grounded prompt to OpenAI.
     │         Temperature = 0.0 → deterministic, factual answers.
     │         Receives the text answer.
     │
     └─[3g] Citation Building + Response
               Maps each of the 5 context chunks into a CitationDTO:
               { documentName, pageNumber, chunkId }

               Returns the final ChatResponse:
               HTTP 200 OK
               {
                 "answer": "The refund policy states that...",
                 "grounded": true,
                 "citations": [
                   { "documentName": "policy.pdf", "pageNumber": 2, "chunkId": "abc-chunk-3" },
                   ...
                 ]
               }
```

---

## 7. API Reference

### Upload Documents

```
POST /chatbot/rag/api/documents/upload
Content-Type: multipart/form-data

Form field: files  (one or more files)
Supported:  application/pdf, text/plain, text/markdown
```

**Success Response — HTTP 202 Accepted**
```json
{
  "message": "Documents uploaded successfully",
  "count": 2,
  "fileNames": ["report.pdf", "notes.txt"]
}
```

**Error Response — HTTP 422 Unprocessable Entity**
```json
{
  "timestamp": "2026-08-23T10:00:00",
  "status": 422,
  "error": "DOCUMENT_PROCESSING_ERROR",
  "message": "Unsupported file type: application/msword",
  "details": []
}
```

---

### Ask a Question

```
POST /chatbot/rag/api/chat
Content-Type: application/json

{
  "message": "Your question here"
}
```

**Success Response — HTTP 200 OK (answer found)**
```json
{
  "answer": "According to the uploaded documents, the refund policy...",
  "grounded": true,
  "citations": [
    {
      "documentName": "policy.pdf",
      "pageNumber": 2,
      "chunkId": "abc-123-chunk-3"
    }
  ]
}
```

**Success Response — HTTP 200 OK (no relevant documents found)**
```json
{
  "answer": "I couldn't find this information in the uploaded documents.",
  "grounded": false,
  "citations": []
}
```

**Error Response — HTTP 400 Bad Request (empty message)**
```json
{
  "timestamp": "2026-08-23T10:00:00",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "Invalid request.",
  "details": ["message: Message is required"]
}
```

---

## 8. Configuration Reference

File: `src/main/resources/application.yaml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ragdb   # PGVector DB URL
    username: raguser
    password: ragpassword

  jpa:
    hibernate:
      ddl-auto: update                            # Auto-create/update tables

  ai:
    openai:
      api-key: ${OPENAI_API_KEY}                  # Set via environment variable

      chat:
        model: gpt-4o-mini                        # LLM for answer generation
        temperature: 0.0                          # 0 = deterministic, factual

      embedding:
        options:
          model: text-embedding-3-small           # Embedding model

    vectorstore:
      pgvector:
        initialize-schema: true                   # Auto-create vector_store table
        index-type: HNSW                          # Best for approximate nearest neighbor
        distance-type: COSINE_DISTANCE            # Measures angle between vectors
        dimensions: 1536                          # Must match text-embedding-3-small output
        max-document-batch-size: 1000

server:
  port: 8080
```

**Tunable RAG Parameters** — `src/main/java/.../util/Constants.java`

| Constant | Value | Meaning |
|---|---|---|
| `DEFAULT_SIMILARITY_THRESHOLD` | `0.75` | Only return chunks with ≥75% cosine similarity to the query |
| `DEFAULT_TOP_K` | `10` | Retrieve up to 10 candidate chunks from PGVector |
| `MAX_CONTEXT_CHUNKS` | `5` | Send only the top 5 chunks to the LLM |
| `DEFAULT_CHUNK_SIZE` | `1000` | Each chunk is at most 1000 characters |
| `DEFAULT_CHUNK_OVERLAP` | `150` | 150-character overlap between consecutive chunks |

---

## 9. Error Handling

All errors are handled by `GlobalExceptionHandler` and returned in a consistent JSON format:

| Exception | HTTP Status | Error Code |
|---|---|---|
| `@NotBlank` / validation failure | 400 Bad Request | `VALIDATION_ERROR` |
| `DocumentProcessingException` | 422 Unprocessable Entity | `DOCUMENT_PROCESSING_ERROR` |
| `RagException` | 503 Service Unavailable | `RAG_ERROR` |
| Any other `Exception` | 500 Internal Server Error | `INTERNAL_SERVER_ERROR` |

---

## 10. Package Structure

```
src/main/java/com/agent/ai_rag_chatbot/
│
├── AiRagChatbotApplication.java        ← Spring Boot entry point
│
├── controller/
│   ├── ChatController.java             ← POST /chatbot/rag/api/chat
│   └── DocumentController.java         ← POST /chatbot/rag/api/documents/upload
│
├── service/
│   ├── ChatService.java                ← Thin orchestration layer for chat
│   ├── RagService.java                 ← Core RAG pipeline (retrieve → prompt → LLM → cite)
│   ├── RetrievalService.java           ← Wraps vector search with RAG constants
│   ├── IngestionService.java           ← Orchestrates parse → chunk → store
│   ├── DocumentParserService.java      ← Selects the right parser by content type
│   └── GroundedPromptService.java      ← Builds the strict system prompt
│
├── vector/
│   ├── VectorStoreService.java         ← Interface: save() and search()
│   └── PgVectorStoreServiceImpl.java   ← PGVector implementation via Spring AI VectorStore
│
├── parser/
│   ├── DocumentParser.java             ← Interface: supports() and parse()
│   ├── PdfDocumentParser.java          ← Apache Tika PDF extraction
│   └── TextDocumentParser.java         ← Plain text / Markdown UTF-8 reading
│
├── chunk/
│   ├── ChunkingService.java            ← Interface: chunk()
│   └── BasicChunkingService.java       ← Sliding window chunker (1000 chars, 150 overlap)
│
├── model/
│   ├── ParsedDocument.java             ← Output of parsers (raw text + metadata)
│   ├── DocumentChunk.java              ← A single text chunk before embedding
│   ├── RetrievedChunk.java             ← A chunk returned from vector search + similarity score
│   └── RagContext.java                 ← Holds question + chunks for prompt building
│
├── dto/
│   ├── ChatRequest.java                ← { message: string }
│   ├── ChatResponse.java               ← { answer, grounded, citations[] }
│   ├── CitationDTO.java                ← { documentName, pageNumber, chunkId }
│   └── DocumentUploadResponse.java     ← { message, count, fileNames[] }
│
├── config/
│   └── AiConfig.java                   ← Registers ChatClient bean
│
├── exception/
│   ├── DocumentProcessingException.java
│   ├── RagException.java
│   ├── GlobalExceptionHandler.java     ← @RestControllerAdvice for all error types
│   └── ApiErrorResponse.java           ← Standard error response shape
│
└── util/
    └── Constants.java                  ← RAG tuning constants and message strings
```
