package com.example.enterprise_rag_chatbot.service;

import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import com.example.enterprise_rag_chatbot.entity.Document;
import com.example.enterprise_rag_chatbot.entity.Chunk;
import com.example.enterprise_rag_chatbot.repository.DocumentRepository;

@Service
public class DocumentIngestionService {

    private final DocumentRepository documentRepository;
    private final EmbeddingService embeddingService;

    public DocumentIngestionService(DocumentRepository documentRepository, EmbeddingService embeddingService) {
        this.documentRepository = documentRepository;
        this.embeddingService = embeddingService;
    }

    public String extractText(String filePath) throws Exception {
        Path path = Path.of(filePath);
        String content = Files.readString(path);
        return content;
    }

    public List<String> splitIntoChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start += (chunkSize - overlap);
        }

        return chunks;
    }
    public Document ingestDocument(String filePath, String filename, String documentType) throws Exception {

        String text = extractText(filePath);

        List<String> textChunks = splitIntoChunks(text, 500, 50);

        Document document = new Document();

        document.setFilename(filename);

        document.setDocumentType(documentType);

        document.setAccessLevel("PUBLIC");

        document.setUploadedAt(LocalDateTime.now());

        List<Chunk> chunkEntities = new ArrayList<>();

        for (int i = 0; i < textChunks.size(); i++) {

            String chunkText = textChunks.get(i);

            float[] embedding = embeddingService.generateEmbedding(chunkText);

            Chunk chunk = new Chunk();

            chunk.setContent(chunkText);

            chunk.setChunkIndex(i);

            chunk.setEmbedding(embedding);

            chunk.setDocument(document);

            chunkEntities.add(chunk);

        }

        document.setChunks(chunkEntities);

        return documentRepository.save(document);

    }

}
