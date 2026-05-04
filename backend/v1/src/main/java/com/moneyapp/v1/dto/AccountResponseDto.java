package com.moneyapp.v1.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import com.moneyapp.v1.enums.AccountType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AccountResponseDto {
    private UUID id;
    private String bankName;
    private AccountType accountType;
    private BigDecimal balance;
    private String currency;
    private boolean active;
    private UUID userId;
    private Date createdAt;
    private Date updatedAt;
    private Date deletedAt;
}
