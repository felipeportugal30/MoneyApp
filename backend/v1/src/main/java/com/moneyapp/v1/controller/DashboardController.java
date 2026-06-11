package com.moneyapp.v1.controller;

import java.time.YearMonth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.moneyapp.v1.dto.DashboardResponseDto;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<DashboardResponseDto> getDashboard(
        @AuthenticationPrincipal User user,
        @RequestParam(required = false) String month
    ) {
        YearMonth yearMonth = month != null ? YearMonth.parse(month) : YearMonth.now();
        return ResponseEntity.ok(dashboardService.getDashboard(user, yearMonth));
    }
}
