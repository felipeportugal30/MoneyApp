package com.moneyapp.v1.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moneyapp.v1.dto.TransactionFilterDto;
import com.moneyapp.v1.model.Transaction;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<List<Transaction>> getTransactions(
            @AuthenticationPrincipal User user,
            @ModelAttribute TransactionFilterDto filter
    ) {
        return ResponseEntity.ok(transactionService.getTransactions(user, filter));
    }
}
