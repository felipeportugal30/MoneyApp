package com.moneyapp.v1.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExtractionService {

    private final RestTemplate restTemplate;

    @Value("${llm.local.url:http://localhost:11434}")
    private String baseUrl;

    @Value("${llm.local.model:llama3}")
    private String model;

    @SuppressWarnings("unchecked")
    public String extractTransactions(String rawText, String fileLanguage) {
        String prompt = buildPrompt(rawText, fileLanguage);

        Map<String, Object> body = Map.of(
            "model",   model,
            "stream",  false,
            "format",  "json",
            "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        Map<String, Object> response = restTemplate.postForObject(
            baseUrl + "/api/chat",
            body,
            Map.class
        );

        Map<String, Object> message = (Map<String, Object>) response.get("message");
        return (String) message.get("content");
    }

    private String buildPrompt(String rawText, String fileLanguage) {
        return """
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
        ALIMENTATION, TRANSPORT, HEALTH, EDUCATION, LEISURE, HOME, SHOPPING, PIX, OTHERS

        Bank statement:
        %s
        """.formatted(fileLanguage, rawText);
    }
}
