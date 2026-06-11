package com.moneyapp.v1.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.moneyapp.v1.dto.CategorySummaryDto;
import com.moneyapp.v1.dto.DashboardResponseDto;
import com.moneyapp.v1.dto.MonthlyEvolutionDto;
import com.moneyapp.v1.dto.RecentTransactionDto;
import com.moneyapp.v1.enums.TransactionType;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.repository.AccountRepository;
import com.moneyapp.v1.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public DashboardResponseDto getDashboard(User user, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        BigDecimal totalBalance = accountRepository.findByUserAndActiveTrue(user).stream()
            .map(a -> a.getBalance() != null ? a.getBalance() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal income = Optional.ofNullable(
            transactionRepository.sumByTypeAndUser(user, TransactionType.INCOME, start, end)
        ).orElse(BigDecimal.ZERO);

        BigDecimal expenses = Optional.ofNullable(
            transactionRepository.sumByTypeAndUser(user, TransactionType.EXPENSE, start, end)
        ).orElse(BigDecimal.ZERO);

        double savingsRate = income.compareTo(BigDecimal.ZERO) > 0
            ? income.subtract(expenses)
                .divide(income, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue()
            : 0.0;

        List<CategorySummaryDto> expensesByCategory = transactionRepository
            .sumByCategoryForUser(user, TransactionType.EXPENSE, start, end)
            .stream()
            .map(row -> new CategorySummaryDto(row[0].toString(), (BigDecimal) row[1]))
            .toList();

        LocalDate evolutionStart = month.minusMonths(5).atDay(1);
        List<Object[]> rawEvolution = transactionRepository.monthlyEvolutionForUser(user, evolutionStart);
        List<MonthlyEvolutionDto> monthlyEvolution = buildMonthlyEvolution(rawEvolution, month);

        List<RecentTransactionDto> recentTransactions = transactionRepository
            .findRecentByUser(user, PageRequest.of(0, 5))
            .stream()
            .map(t -> new RecentTransactionDto(
                t.getId(), t.getAmount(), t.getDate(),
                t.getType().name(), t.getCategory().name(), t.getDescription()
            ))
            .toList();

        return new DashboardResponseDto(
            totalBalance, income, expenses, savingsRate,
            expensesByCategory, monthlyEvolution, recentTransactions
        );
    }

    private List<MonthlyEvolutionDto> buildMonthlyEvolution(List<Object[]> raw, YearMonth current) {
        Map<String, BigDecimal[]> map = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            map.put(current.minusMonths(i).toString(), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        }

        for (Object[] row : raw) {
            int year = ((Number) row[0]).intValue();
            int monthNum = ((Number) row[1]).intValue();
            TransactionType type = (TransactionType) row[2];
            BigDecimal amount = (BigDecimal) row[3];

            String key = YearMonth.of(year, monthNum).toString();
            if (map.containsKey(key)) {
                if (type == TransactionType.INCOME) {
                    map.get(key)[0] = amount;
                } else {
                    map.get(key)[1] = amount;
                }
            }
        }

        return map.entrySet().stream()
            .map(e -> new MonthlyEvolutionDto(e.getKey(), e.getValue()[0], e.getValue()[1]))
            .toList();
    }
}
