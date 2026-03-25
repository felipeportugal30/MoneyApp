package com.moneyapp.v1.dto;

import com.moneyapp.v1.enums.ExpenseCategory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UpdateTransactionDto {
    private ExpenseCategory category;
}
