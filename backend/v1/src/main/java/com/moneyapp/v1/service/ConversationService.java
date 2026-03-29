package com.moneyapp.v1.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.moneyapp.v1.dto.SendMessageRequestDto;
import com.moneyapp.v1.dto.SendMessageResponseDto;
import com.moneyapp.v1.model.Conversation;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.repository.ConversationRepository;
import com.moneyapp.v1.service.llm.LLMProviderFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final LLMProviderFactory llmFactory;

    public SendMessageResponseDto sendMessage(User requestUser, SendMessageRequestDto request) {

        List<Conversation> historico = conversationRepository
            .findByUserIdAndDeletedAtIsNullOrderByCreatedAtAsc(requestUser.getId());

        List<Map<String, String>> messages = new ArrayList<>();
        for (Conversation c : historico) {
            messages.add(Map.of("role", "user",      "content", c.getMessage()));
            messages.add(Map.of("role", "assistant", "content", c.getAnswer()));
        }
        messages.add(Map.of("role", "user", "content", request.getMessage()));

        String systemPrompt = buildSystemPrompt();

        String answer = llmFactory.getProvider().chat(systemPrompt, messages);

        Conversation conv = new Conversation();
        conv.setMessage(request.getMessage());
        conv.setAnswer(answer);
        conv.setUser(requestUser);
        conversationRepository.save(conv);

        return new SendMessageResponseDto(request.getMessage(), answer, conv.getCreatedAt(), true);
    }

    private String buildSystemPrompt() {
        return """
            You are a personal financial advisor. Your role is to analyze the user's
            financial data and provide clear, practical, and encouraging advice.
            
            Rules:
            - Always respond in the same language the user writes in.
            - Be specific with monetary values from the data provided.
            - Never invent data that is not in the context below.
            - Be concise and objective.
            
            === User financial data ===
            ...
            ===========================
            """;
    }
}