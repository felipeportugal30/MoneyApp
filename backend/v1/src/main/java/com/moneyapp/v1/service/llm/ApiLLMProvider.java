package com.moneyapp.v1.service.llm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service("api")
@RequiredArgsConstructor
public class ApiLLMProvider implements LLMProvider {
    
    private final RestTemplate restTemplate;

    @Value("${llm.api.url:https://api.groq.com/openai/v1}")
    private String baseUrl;

    @Value("${llm.api.model:llama3-8b-8192}")
    private String model;

    @Value("${llm.api.key}")
    private String apiKey;

        @Override
    public String chat(String systemPrompt, List<Map<String, String>> messages) {
        List<Map<String, String>> payload = new ArrayList<>();
        payload.add(Map.of("role", "system", "content", systemPrompt));
        payload.addAll(messages);

        Map<String, Object> body = Map.of(
            "model",    model,
            "messages", payload
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(
            baseUrl + "/chat/completions",
            new HttpEntity<>(body, headers),
            Map.class
        );

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");

        @SuppressWarnings("unchecked")
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

        return (String) message.get("content");
    }
}
