package com.moneyapp.v1.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;


@Service
public class GroqService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.groq.com/openai/v1")
            .build();

    @SuppressWarnings("unchecked")
    public String extractTransactions(String rawText, String file_language) {
        String prompt = """
        You are an AI specialized in financial data extraction.

        I will provide a bank statement in %s. Your task is to:

        1. Extract ALL transactions, including expenses, PIX payments, income, and salaries.
        2. For each transaction identify:
            - description: merchant name or transaction description (normalize it, remove codes)
            - amount: numeric value (use dot as decimal separator, no currency symbol)
            - date: convert to DD/MM/YYYY format
            - category: MUST be one of the predefined categories below
            - type: either "EXPENSE" or "INCOME"
        
        3. Category rules (follow strictly):
            - PIX payments to people or unknown recipients → category: PIX
            - Credit card bill without itemized details → category: OTHERS
            - Salary, wages, investment returns, dividends → category: INCOME, type: INCOME
            - All other income (cashback, refunds) → category: INCOME, type: INCOME
            - Groceries, restaurants, food delivery → category: ALIMENTATION
            - Uber, gas, public transport → category: TRANSPORT
            - Hospitals, pharmacies, doctors → category: HEALTH
            - Courses, books, schools → category: EDUCATION
            - Streaming, games, cinema → category: LEISURE
            - Rent, electricity, water, internet → category: HOME
            - Clothing, electronics, general shopping → category: SHOPPING
            - Anything else unclear → category: OTHERS

        4. Translate all descriptions to English.
        5. Do NOT include duplicate transactions.
        6. Return ONLY raw JSON, no markdown, no backticks, no explanations.
        
        Strictly follow this JSON structure:
        {
            "transactions": [
                {
                "description": "string",
                "amount": 0.00,
                "date": "DD/MM/YYYY",
                "category": "string",
                "type": "EXPENSE or INCOME"
                }
            ]
        }

        Rules:
        - Output MUST be valid JSON.
        - Do not include any text outside the JSON.
        - Do not include trailing commas.
        - Use dot as decimal separator (e.g., 10.50).
        - Do NOT return numbers as strings.
        - If a field is missing, infer it when possible.
        - Do not include duplicate transactions.
        - Normalize merchant names (remove codes, extra numbers, etc.).

        Allowed categories (use ONLY these):
        ALIMENTATION, TRANSPORT, HEALTH, EDUCATION, LEISURE, HOME, SHOPPING, PIX and OTHERS

        IMPORTANT: Return ONLY raw JSON. Do NOT wrap in markdown code blocks. Do NOT use backticks.

        Bank statement:
        %s
        """
        .formatted(file_language, rawText);

        Map<String, Object> body = Map.of(
            "model", "llama-3.3-70b-versatile",
            "temperature", 0,
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            )
        );

        Map<String, Object> response = webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}