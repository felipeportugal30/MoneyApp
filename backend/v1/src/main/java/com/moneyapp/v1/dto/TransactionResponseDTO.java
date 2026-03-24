package com.moneyapp.v1.dto;

import java.util.List;

public record TransactionResponseDTO(
    List<TransactionItemDTO> transactions
) {}