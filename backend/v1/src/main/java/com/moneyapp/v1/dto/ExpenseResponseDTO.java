package com.moneyapp.v1.dto;

import java.util.List;

public record ExpenseResponseDTO(
    List<ExpenseItemDTO> transactions
) {}