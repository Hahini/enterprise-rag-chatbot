package com.example.enterprise_rag_chatbot.service;

import com.example.enterprise_rag_chatbot.dto.SearchResult;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenerationService {

    private final ChatClient chatClient;

    public GenerationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String generateAnswer(String question, List<SearchResult> context, List<ChatMessage> history) {

        String contextText = context.stream()
                .map(r -> "[Source " + r.id() + "]: " + r.content())
                .collect(Collectors.joining("\n\n"));

        String historyText = history.isEmpty()
                ? "(no previous conversation)"
                : history.stream()
                    .map(m -> m.role() + ": " + m.content())
                    .collect(Collectors.joining("\n"));

       String prompt = """
You are an assistant answering questions about a Vitality/insurance program.

Previous conversation:
%s

Sources:
%s

Question:
%s

Instructions:
- Use the previous conversation only to understand context (e.g. what "it" or "that" refers to).
- Answer ONLY using the information in the provided sources.
- Never use your own knowledge.
- If the answer cannot be found in the sources, reply exactly:
  "I couldn't find relevant information in the uploaded documents."
- Do not guess.
- Do not make assumptions.
- After your answer, list which source IDs you used, like:
  "Sources used: [21, 20]"
""".formatted(historyText, contextText, question);

        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }
}