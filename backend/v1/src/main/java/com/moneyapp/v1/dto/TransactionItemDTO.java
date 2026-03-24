package com.moneyapp.v1.dto;

import java.math.BigDecimal;

public record TransactionItemDTO(
    BigDecimal amount,
    String date,
    String category,
    String description,
    String type
) {}