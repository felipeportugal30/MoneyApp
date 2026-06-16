package com.moneyapp.v1.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionDto(
    UUID id,
    BigDecimal amount,
    String date,
    String type,
    String category,
    String description
) {}
