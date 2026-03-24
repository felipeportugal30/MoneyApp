package com.moneyapp.v1.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.moneyapp.v1.enums.ExpenseCategory;
import com.moneyapp.v1.enums.TransactionType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionFilterDto {
    private ExpenseCategory category;
    private TransactionType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID userId;
    private Boolean allUsers;
}
