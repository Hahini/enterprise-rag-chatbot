package com.example.enterprise_rag_chatbot.repository;

import com.example.enterprise_rag_chatbot.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}