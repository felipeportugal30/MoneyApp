package com.moneyapp.v1.service.llm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service("local")
@RequiredArgsConstructor
public class LocalLLMProvider implements LLMProvider {

    private final RestTemplate restTemplate;

    @Value("${llm.local.url:http://localhost:11434}")
    private String baseUrl;

    @Value("${llm.local.model:llama3}")
    private String model;

    @Override
    public String chat(String systemPrompt, List<Map<String, String>> messages) {
        List<Map<String, String>> payload = new ArrayList<>();
        payload.add(Map.of("role", "system", "content", systemPrompt));
        payload.addAll(messages);

        Map<String, Object> body = Map.of(
            "model",    model,
            "stream",   false,
            "messages", payload
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
            baseUrl + "/api/chat",
            body,
            Map.class
        );

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) response.get("message");

        return (String) message.get("content");
    }
}