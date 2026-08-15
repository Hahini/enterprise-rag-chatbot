package com.example.enterprise_rag_chatbot.service;

import com.example.enterprise_rag_chatbot.dto.SearchResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class RerankingService {

    private final ChatClient chatClient;

    public RerankingService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public List<SearchResult> rerank(String question, List<SearchResult> candidates) {

        List<ScoredResult> scored = candidates.stream()
                .map(result -> new ScoredResult(result, scoreRelevance(question, result.content())))
                .sorted(Comparator.comparingInt((ScoredResult sr) -> sr.score).reversed())
                .toList();

        return scored.stream()
                .map(sr -> sr.result)
                .toList();
    }

   private int scoreRelevance(String question, String passage) {

    String prompt = """
            Query: %s
            Passage: %s

            Rate how relevant this passage is to answering the query, on a scale of 0-10.
            Respond with ONLY the number, nothing else.
            """.formatted(question, passage);

    try {
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        System.out.println("RERANK RAW RESPONSE: [" + response + "]");

        return Integer.parseInt(response.trim());
    } catch (Exception e) {
        System.out.println("RERANK FAILED: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        return 5;
    }
}

    private record ScoredResult(SearchResult result, int score) {}
}