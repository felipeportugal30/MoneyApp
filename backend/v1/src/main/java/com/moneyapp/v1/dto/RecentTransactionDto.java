package com.moneyapp.v1.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecentTransactionDto(
    UUID id,
    BigDecimal amount,
    LocalDate date,
    String type,
    String category,
    String description
) {}
