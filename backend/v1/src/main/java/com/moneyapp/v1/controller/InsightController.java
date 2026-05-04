package com.moneyapp.v1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moneyapp.v1.dto.InsightResponseDto;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.service.FinancialInsightService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/insights")
@RequiredArgsConstructor
public class InsightController {

    private final FinancialInsightService financialInsightService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<InsightResponseDto> getInsights(
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(financialInsightService.generateInsights(user));
    }
}
