package com.moneyapp.v1.service.llm;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LLMProviderFactory {
    
    @Value("${llm.provider:ollama}")
    private String providerName;

    private final Map<String, LLMProvider> providers;

    public LLMProviderFactory(Map<String, LLMProvider> providers) {
        this.providers = providers;
    }

    public LLMProvider getProvider() {
        LLMProvider provider = providers.get(providerName);
        if (provider == null) {
            throw new IllegalStateException(
                "Unknown provider: " + providerName + 
                ". Use: local or api"
            );
        }
        return provider;
    }
}
