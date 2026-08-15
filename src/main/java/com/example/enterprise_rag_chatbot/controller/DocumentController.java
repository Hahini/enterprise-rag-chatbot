package com.example.enterprise_rag_chatbot.controller;

import com.example.enterprise_rag_chatbot.dto.SearchResult;
import com.example.enterprise_rag_chatbot.entity.Chunk;
import com.example.enterprise_rag_chatbot.service.DocumentIngestionService;
import com.example.enterprise_rag_chatbot.service.EmbeddingService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentIngestionService ingestionService;
    private final EmbeddingService embeddingService;

    public DocumentController(DocumentIngestionService ingestionService, EmbeddingService embeddingService) {
        this.ingestionService = ingestionService;
        this.embeddingService = embeddingService;
    }

    @PostMapping("/test-extract")
    public String testExtract(@RequestParam String filePath) throws Exception {
        return ingestionService.extractText(filePath);
    }

    @PostMapping("/test-chunk")
    public List<String> testChunk(@RequestParam String filePath) throws Exception {
        String text = ingestionService.extractText(filePath);
        return ingestionService.splitIntoChunks(text, 500, 50);
    }

    @PostMapping("/test-embed")
    public float[] testEmbed(@RequestParam String text) {
        return embeddingService.generateEmbedding(text);
    }
    @PostMapping("/ingest")
public String ingest(@RequestParam String filePath, @RequestParam String filename, @RequestParam String documentType) throws Exception {
    var document = ingestionService.ingestDocument(filePath, filename, documentType);
    return "Ingested document with ID: " + document.getId() + ", chunks: " + document.getChunks().size();
}
@PostMapping("/search")
public List<SearchResult> search(
        @RequestParam String question,
        @RequestParam(defaultValue = "5") int topK) {

    return ingestionService.searchSimilarChunks(question, topK);
}
@PostMapping("/hybrid-search")
public List<SearchResult> hybridSearch(
        @RequestParam String question,
        @RequestParam(defaultValue = "5") int topK) {

    return ingestionService.hybridSearch(question, topK);
}
@PostMapping("/search-reranked")
public List<SearchResult> hybridSearchWithRerank(
        @RequestParam String question,
        @RequestParam(defaultValue = "5") int topK) {

    return ingestionService.hybridSearchWithRerank(question, topK);
}
@PostMapping("/answer")
public String answer(
        @RequestParam String conversationId,
        @RequestParam String question,
        @RequestParam(defaultValue = "5") int topK) {

    return ingestionService.answerQuestion(conversationId, question, topK);
}
}