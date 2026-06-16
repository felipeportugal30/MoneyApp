package com.moneyapp.v1.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.moneyapp.v1.dto.InsightResponseDto;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.service.llm.LLMProviderFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinancialInsightService {

    private final FinancialContextService financialContextService;
    private final LLMProviderFactory llmProviderFactory;

    private static final String SYSTEM_PROMPT =
        "Você é um consultor financeiro pessoal direto e empático. " +
        "O usuário vai te enviar um resumo dos gastos do mês atual comparado com a média dos últimos 3 meses. " +
        "Sua tarefa: identificar pontos de atenção, destacar categorias com aumento relevante (acima de 20%), " +
        "elogiar onde houve economia, e dar 2 ou 3 conselhos práticos e objetivos. " +
        "Responda em português, de forma concisa (máximo 5 parágrafos). Não repita os números — interprete-os.";

    public InsightResponseDto generateInsights(User user) {
        String context = financialContextService.buildContext(user);

        String insights = llmProviderFactory.getProvider().chat(
            SYSTEM_PROMPT,
            List.of(Map.of("role", "user", "content", context))
        );

        String period = LocalDate.now().format(DateTimeFormatter.ofPattern("MM/yyyy"));
        return new InsightResponseDto(period, insights);
    }
}
