package com.example.enterprise_rag_chatbot.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class ConversationMemoryService {

    // conversationId -> list of messages
    private final Map<String, List<ChatMessage>> conversations = new ConcurrentHashMap<>();

    private static final int MAX_TURNS = 5; // keep last 5 Q&A pairs

    public List<ChatMessage> getHistory(String conversationId) {
        return conversations.getOrDefault(conversationId, List.of());
    }

    public void addTurn(String conversationId, String userMessage, String assistantMessage) {
        List<ChatMessage> history = conversations.computeIfAbsent(conversationId, k -> new CopyOnWriteArrayList<>());
        history.add(new ChatMessage("user", userMessage));
        history.add(new ChatMessage("assistant", assistantMessage));

        // trim to last MAX_TURNS*2 messages (user+assistant pairs)
        while (history.size() > MAX_TURNS * 2) {
            history.remove(0);
        }
    }
}