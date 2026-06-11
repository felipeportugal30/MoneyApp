package com.moneyapp.v1.dto;

import java.math.BigDecimal;

public record MonthlyEvolutionDto(String month, BigDecimal income, BigDecimal expenses) {}
