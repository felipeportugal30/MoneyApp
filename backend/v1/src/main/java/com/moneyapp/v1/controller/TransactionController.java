package com.moneyapp.v1.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.moneyapp.v1.dto.TransactionFilterDto;
import com.moneyapp.v1.dto.UpdateTransactionDto;
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

    @PutMapping("/{transaction_id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<Transaction> updateTransactionCategory(
        @AuthenticationPrincipal User user,
        @PathVariable UUID transaction_id,
        @RequestBody UpdateTransactionDto request
    ) {
        return ResponseEntity.ok(transactionService.updateTransaction(user, transaction_id, request));
    }

    @DeleteMapping("/{transaction_id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'MODERATOR')")
    public ResponseEntity<Transaction> deleteTransaction(
        @AuthenticationPrincipal User user,
        @PathVariable UUID transaction_id
    ) {
        return ResponseEntity.ok(transactionService.deleteTransaction(user, transaction_id));
    }
}
