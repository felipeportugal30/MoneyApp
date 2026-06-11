package com.moneyapp.v1.dto;

import java.math.BigDecimal;

public record CategorySummaryDto(String category, BigDecimal total) {}
