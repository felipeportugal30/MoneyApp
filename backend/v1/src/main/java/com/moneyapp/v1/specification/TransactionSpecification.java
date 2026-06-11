package com.moneyapp.v1.specification;

import com.moneyapp.v1.enums.ExpenseCategory;
import com.moneyapp.v1.enums.TransactionType;
import com.moneyapp.v1.model.Transaction;
import com.moneyapp.v1.model.User;
import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.util.UUID;

public class TransactionSpecification {

    public static Specification<Transaction> byUser(User user) {
        return (root, query, cb) -> cb.equal(root.get("account").get("user"), user);
    }

    public static Specification<Transaction> byUserId(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("account").get("user").get("id"), userId);
    }

    public static Specification<Transaction> byCategory(ExpenseCategory category) {
        return (root, query, cb) -> category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Transaction> byType(TransactionType type) {
        return (root, query, cb) -> type == null ? null : cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> byDateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) return null;
            if (start == null) return cb.lessThanOrEqualTo(root.get("date"), end);
            if (end == null) return cb.greaterThanOrEqualTo(root.get("date"), start);
            return cb.between(root.get("date"), start, end);
        };
    }
}