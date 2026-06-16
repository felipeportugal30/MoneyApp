package com.moneyapp.v1.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.moneyapp.v1.enums.ExpenseCategory;
import com.moneyapp.v1.enums.TransactionType;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinancialContextService {

    private final TransactionRepository transactionRepository;

    public String buildContext(User user) {
        LocalDate today = LocalDate.now();
        LocalDate startCurrentMonth = today.withDayOfMonth(1);
        LocalDate startLast3Months = today.minusMonths(3).withDayOfMonth(1);
        LocalDate endLast3Months = startCurrentMonth.minusDays(1);

        Map<ExpenseCategory, BigDecimal> currentExpenses = queryByCategory(
            user, TransactionType.EXPENSE, startCurrentMonth, today
        );
        Map<ExpenseCategory, BigDecimal> last3MonthsExpenses = queryByCategory(
            user, TransactionType.EXPENSE, startLast3Months, endLast3Months
        );
        Map<ExpenseCategory, BigDecimal> currentIncome = queryByCategory(
            user, TransactionType.INCOME, startCurrentMonth, today
        );

        return buildContextString(currentExpenses, last3MonthsExpenses, currentIncome, today);
    }

    private Map<ExpenseCategory, BigDecimal> queryByCategory(
        User user, TransactionType type, LocalDate start, LocalDate end
    ) {
        List<Object[]> rows = transactionRepository.sumByCategoryForUser(user, type, start, end);
        Map<ExpenseCategory, BigDecimal> result = new EnumMap<>(ExpenseCategory.class);
        for (Object[] row : rows) {
            result.put((ExpenseCategory) row[0], (BigDecimal) row[1]);
        }
        return result;
    }

    private String buildContextString(
        Map<ExpenseCategory, BigDecimal> currentExpenses,
        Map<ExpenseCategory, BigDecimal> last3MonthsExpenses,
        Map<ExpenseCategory, BigDecimal> currentIncome,
        LocalDate today
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("Financial report — ").append(today.format(DateTimeFormatter.ofPattern("MMMM/yyyy"))).append("\n\n");
        sb.append("Current month expenses vs. 3-month average:\n");

        BigDecimal totalCurrentExpenses = BigDecimal.ZERO;
        BigDecimal totalCurrentIncome = BigDecimal.ZERO;

        for (ExpenseCategory category : ExpenseCategory.values()) {
            if (category == ExpenseCategory.INCOME) continue;

            BigDecimal current = currentExpenses.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal last3Total = last3MonthsExpenses.getOrDefault(category, BigDecimal.ZERO);
            BigDecimal avg = last3Total.divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);

            totalCurrentExpenses = totalCurrentExpenses.add(current);

            String change = formatChange(current, avg);
            sb.append(String.format("  %-15s R$ %8.2f  |  Avg: R$ %8.2f  %s%n",
                category.name(), current, avg, change));
        }

        for (BigDecimal income : currentIncome.values()) {
            totalCurrentIncome = totalCurrentIncome.add(income);
        }

        BigDecimal balance = totalCurrentIncome.subtract(totalCurrentExpenses);

        sb.append("\n");
        sb.append(String.format("Total expenses:  R$ %.2f%n", totalCurrentExpenses));
        sb.append(String.format("Total income:    R$ %.2f%n", totalCurrentIncome));
        sb.append(String.format("Monthly balance: R$ %.2f%n", balance));

        return sb.toString();
    }

    private String formatChange(BigDecimal current, BigDecimal avg) {
        if (avg.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? "(new)" : "";
        }
        BigDecimal pct = current.subtract(avg)
            .divide(avg, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
        String sign = pct.compareTo(BigDecimal.ZERO) > 0 ? "+" : "";
        return String.format("(%s%.1f%%)", sign, pct);
    }
}
