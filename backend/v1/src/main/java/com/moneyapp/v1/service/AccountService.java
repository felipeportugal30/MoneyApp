package com.moneyapp.v1.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.moneyapp.v1.dto.AccountRequestDto;
import com.moneyapp.v1.dto.AccountResponseDto;
import com.moneyapp.v1.dto.AccountUpdateRequestDto;
import com.moneyapp.v1.exception.InvalidRequestException;
import com.moneyapp.v1.exception.NotFoundException;
import com.moneyapp.v1.exception.UnauthorizedException;
import com.moneyapp.v1.model.Account;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.repository.AccountRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountResponseDto createAccount(AccountRequestDto request, User user) {
        boolean alreadyExists = accountRepository
            .findByUserAndBankNameAndAccountType(user, request.getBankName(), request.getAccountType())
            .isPresent();

        if (alreadyExists) {
            throw new InvalidRequestException(
                "You already have a " + request.getAccountType() + " account at " + request.getBankName()
            );
        }

        Account account = new Account();
        account.setUser(user);
        account.setBankName(request.getBankName());
        account.setAccountType(request.getAccountType());
        account.setCurrency(request.getCurrency().toUpperCase());
        account.setBalance(request.getBalance());

        Account saved = accountRepository.save(account);
        return toResponseDto(saved);
    }

    public List<AccountResponseDto> listAccounts(User user) {
        return accountRepository.findByUserAndActiveTrue(user)
            .stream()
            .map(this::toResponseDto)
            .collect(Collectors.toList());
    }

    public AccountResponseDto getAccount(UUID accountId, User user) {
        Account account = findAccountOwnedByUser(accountId, user);
        return toResponseDto(account);
    }

    public AccountResponseDto updateAccount(UUID accountId, AccountUpdateRequestDto request, User user) {
        Account account = findAccountOwnedByUser(accountId, user);

        String newBankName = request.getBankName() != null ? request.getBankName() : account.getBankName();
        var newAccountType = request.getAccountType() != null ? request.getAccountType() : account.getAccountType();

        boolean changed = !newBankName.equals(account.getBankName()) || newAccountType != account.getAccountType();
        if (changed) {
            boolean conflict = accountRepository
                .findByUserAndBankNameAndAccountType(user, newBankName, newAccountType)
                .isPresent();
            if (conflict) {
                throw new InvalidRequestException(
                    "You already have a " + newAccountType + " account at " + newBankName
                );
            }
        }

        account.setBankName(newBankName);
        account.setAccountType(newAccountType);
        if (request.getCurrency() != null) account.setCurrency(request.getCurrency().toUpperCase());
        if (request.getBalance() != null) account.setBalance(request.getBalance());
        account.setUpdatedAt(new Date());

        return toResponseDto(accountRepository.save(account));
    }

    public void deleteAccount(UUID accountId, User user) {
        Account account = findAccountOwnedByUser(accountId, user);
        account.setActive(false);
        account.setDeletedAt(new Date());
        accountRepository.save(account);
    }

    private Account findAccountOwnedByUser(UUID accountId, User user) {
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Access denied");
        }

        return account;
    }

    private AccountResponseDto toResponseDto(Account account) {
        return new AccountResponseDto(
            account.getId(),
            account.getBankName(),
            account.getAccountType(),
            account.getBalance(),
            account.getCurrency(),
            account.isActive(),
            account.getUser().getId(),
            account.getCreatedAt(),
            account.getUpdatedAt(),
            account.getDeletedAt()
        );
    }
}
