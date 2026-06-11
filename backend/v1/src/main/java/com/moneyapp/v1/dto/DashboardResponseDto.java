package com.moneyapp.v1.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponseDto(
    BigDecimal totalBalance,
    BigDecimal monthlyIncome,
    BigDecimal monthlyExpenses,
    double savingsRate,
    List<CategorySummaryDto> expensesByCategory,
    List<MonthlyEvolutionDto> monthlyEvolution,
    List<RecentTransactionDto> recentTransactions
) {}
