package com.example.enterprise_rag_chatbot.dto;

public record SearchResult(
        Long id,
        String content,
        Integer chunkIndex
) {}