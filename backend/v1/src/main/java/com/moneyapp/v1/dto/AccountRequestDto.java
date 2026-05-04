package com.moneyapp.v1.dto;

import java.math.BigDecimal;

import com.moneyapp.v1.enums.AccountType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AccountRequestDto {

    @NotBlank(message = "Bank name is required")
    private String bankName;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code (e.g. BRL, USD)")
    @Pattern(regexp = "[A-Z]{3}", message = "Currency must be uppercase (e.g. BRL, USD)")
    private String currency;

    private BigDecimal balance;
}
