package com.moneyapp.v1.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.moneyapp.v1.dto.AccountRequestDto;
import com.moneyapp.v1.dto.AccountResponseDto;
import com.moneyapp.v1.dto.AccountUpdateRequestDto;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AccountResponseDto> createAccount(
        @AuthenticationPrincipal User user,
        @Valid @RequestBody AccountRequestDto request
    ) {
        return ResponseEntity.status(201).body(accountService.createAccount(request, user));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<AccountResponseDto>> listAccounts(
        @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(accountService.listAccounts(user));
    }

    @GetMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AccountResponseDto> getAccount(
        @AuthenticationPrincipal User user,
        @PathVariable UUID accountId
    ) {
        return ResponseEntity.ok(accountService.getAccount(accountId, user));
    }

    @PutMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AccountResponseDto> updateAccount(
        @AuthenticationPrincipal User user,
        @PathVariable UUID accountId,
        @Valid @RequestBody AccountUpdateRequestDto request
    ) {
        return ResponseEntity.ok(accountService.updateAccount(accountId, request, user));
    }

    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteAccount(
        @AuthenticationPrincipal User user,
        @PathVariable UUID accountId
    ) {
        accountService.deleteAccount(accountId, user);
        return ResponseEntity.noContent().build();
    }
}
