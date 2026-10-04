package com.example.enterprise_rag_chatbot.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(columnDefinition = "TEXT")
    private String question;

    @Column(columnDefinition = "TEXT")
    private String retrievedChunkIds;

    @Column(columnDefinition = "TEXT")
    private String answer;

    private LocalDateTime timestamp;

    public AuditLog() {}

    public AuditLog(String username, String question, String retrievedChunkIds, String answer, LocalDateTime timestamp) {
        this.username = username;
        this.question = question;
        this.retrievedChunkIds = retrievedChunkIds;
        this.answer = answer;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getQuestion() { return question; }
    public String getRetrievedChunkIds() { return retrievedChunkIds; }
    public String getAnswer() { return answer; }
    public LocalDateTime getTimestamp() { return timestamp; }
}