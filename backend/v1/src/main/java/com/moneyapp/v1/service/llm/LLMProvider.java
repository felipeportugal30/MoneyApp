package com.moneyapp.v1.service.llm;

import java.util.List;
import java.util.Map;

public interface LLMProvider {

    String chat(String systemPrompt, List<Map<String, String>> message);    
} 
