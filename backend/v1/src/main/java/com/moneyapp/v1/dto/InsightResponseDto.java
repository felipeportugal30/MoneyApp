package com.moneyapp.v1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InsightResponseDto {
    private String period;
    private String insights;
}
