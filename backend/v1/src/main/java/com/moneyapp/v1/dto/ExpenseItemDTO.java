package com.moneyapp.v1.dto;

import java.math.BigDecimal;

public record ExpenseItemDTO(
    BigDecimal amount,
    String date,
    String category,
    String description,
    String type
) {}