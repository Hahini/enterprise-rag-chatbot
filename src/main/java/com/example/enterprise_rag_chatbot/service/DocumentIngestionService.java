package com.example.enterprise_rag_chatbot.service;

import org.springframework.stereotype.Service;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import com.example.enterprise_rag_chatbot.entity.Document;
import com.example.enterprise_rag_chatbot.dto.SearchResult;
import com.example.enterprise_rag_chatbot.entity.Chunk;
import com.example.enterprise_rag_chatbot.repository.ChunkRepository;
import com.example.enterprise_rag_chatbot.repository.DocumentRepository;

@Service
public class DocumentIngestionService {

    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;
    private final RerankingService rerankingService;
    private final GenerationService generationService;
    private final ConversationMemoryService memoryService;


    public DocumentIngestionService(DocumentRepository documentRepository, ChunkRepository chunkRepository, EmbeddingService embeddingService,  RerankingService rerankingService,GenerationService generationService, ConversationMemoryService memoryService) {
    this.documentRepository = documentRepository;
    this.chunkRepository = chunkRepository;
    this.embeddingService = embeddingService;
      this.rerankingService = rerankingService;
      this.generationService = generationService;
      this.memoryService = memoryService;
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
public List<SearchResult> searchSimilarChunks(String question, int topK) {

    float[] embedding = embeddingService.generateEmbedding(question);

    String vector = embeddingService.embeddingToString(embedding);

    List<Object[]> rows = chunkRepository.findNearestChunks(vector, topK);

    return rows.stream()
            .map(r -> new SearchResult(
                    ((Number) r[0]).longValue(),
                    (String) r[1],
                    ((Number) r[2]).intValue()))
            .toList();
}
public List<SearchResult> searchByKeyword(String question, int topK) {

    List<Object[]> rows = chunkRepository.findByKeywordSearch(question, topK);

    return rows.stream()
            .map(r -> new SearchResult(
                    ((Number) r[0]).longValue(),
                    (String) r[1],
                    ((Number) r[2]).intValue()))
            .toList();
}

public List<SearchResult> hybridSearch(String question, int topK) {

    List<SearchResult> vectorResults = searchSimilarChunks(question, topK);
    List<SearchResult> keywordResults = searchByKeyword(question, topK);

    return mergeResults(vectorResults, keywordResults, topK);
}
private List<SearchResult> mergeResults(List<SearchResult> vectorResults, List<SearchResult> keywordResults, int topK) {

    java.util.Map<Long, Double> scores = new java.util.HashMap<>();
    java.util.Map<Long, SearchResult> resultById = new java.util.HashMap<>();

    for (int i = 0; i < vectorResults.size(); i++) {
        SearchResult r = vectorResults.get(i);
        double score = (vectorResults.size() - i) * 0.7;
        scores.merge(r.id(), score, Double::sum);
        resultById.put(r.id(), r);
    }

    for (int i = 0; i < keywordResults.size(); i++) {
        SearchResult r = keywordResults.get(i);
        double score = (keywordResults.size() - i) * 0.3;
        scores.merge(r.id(), score, Double::sum);
        resultById.put(r.id(), r);
    }

    return scores.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(topK)
            .map(e -> resultById.get(e.getKey()))
            .toList();
}public List<SearchResult> hybridSearchWithRerank(String question, int topK) {

    int candidatePoolSize = topK * 2;

    List<SearchResult> candidates = hybridSearch(question, candidatePoolSize);

    List<SearchResult> reranked = rerankingService.rerank(question, candidates);

    return reranked.stream().limit(topK).toList();
}
public String answerQuestion(String conversationId, String question, int topK) {
    List<SearchResult> context = hybridSearchWithRerank(question, topK);
    List<ChatMessage> history = memoryService.getHistory(conversationId);

    String answer = generationService.generateAnswer(question, context, history);

    memoryService.addTurn(conversationId, question, answer);

    return answer;
}
}
