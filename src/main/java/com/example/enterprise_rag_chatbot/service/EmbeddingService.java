package com.example.enterprise_rag_chatbot.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingService {

    private final RestTemplate restTemplate = new RestTemplate();

    public float[] generateEmbedding(String text) {
        String url = "http://localhost:11434/api/embeddings";

        Map<String, String> requestBody = Map.of(
            "model", "nomic-embed-text",
            "prompt", text
        );

        Map<String, Object> response = restTemplate.postForObject(url, requestBody, Map.class);

        List<Double> embeddingList = (List<Double>) response.get("embedding");

        float[] embedding = new float[embeddingList.size()];
        for (int i = 0; i < embeddingList.size(); i++) {
            embedding[i] = embeddingList.get(i).floatValue();
        }

        return embedding;
    }
}