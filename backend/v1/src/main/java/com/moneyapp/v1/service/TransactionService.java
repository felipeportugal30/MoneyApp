package com.moneyapp.v1.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.moneyapp.v1.dto.TransactionDto;
import com.moneyapp.v1.dto.TransactionFilterDto;
import com.moneyapp.v1.dto.TransactionResponseDTO;
import com.moneyapp.v1.dto.UpdateTransactionDto;
import com.moneyapp.v1.enums.ExpenseCategory;
import com.moneyapp.v1.enums.Role;
import com.moneyapp.v1.enums.TransactionType;
import com.moneyapp.v1.exception.InvalidRequestException;
import com.moneyapp.v1.exception.NotFoundException;
import com.moneyapp.v1.exception.UnauthorizedException;
import com.moneyapp.v1.model.Account;
import com.moneyapp.v1.model.File;
import com.moneyapp.v1.model.Transaction;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.repository.TransactionRepository;
import com.moneyapp.v1.specification.TransactionSpecification;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<TransactionDto> saveTransactions(String json, File file, Account account, User user) throws Exception {
        String cleanJson = json
            .replaceAll("```json", "")
            .replaceAll("```", "")
            .trim();

        TransactionResponseDTO response;
        try {
            response = objectMapper.readValue(cleanJson, TransactionResponseDTO.class);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestException("Error parsing AI response: " + e.getMessage());
        }

        List<Transaction> transactions = response.transactions().stream().map(item -> {
            Transaction transaction = new Transaction();
            transaction.setAmount(item.amount());
            transaction.setDate(LocalDate.parse(item.date(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            transaction.setType(TransactionType.valueOf(item.type()));
            transaction.setCategory(ExpenseCategory.valueOf(item.category()));
            transaction.setDescription(item.description());
            transaction.setFile(file);
            transaction.setAccount(account);
            return transaction;
        }).toList();

        return transactionRepository.saveAll(transactions).stream().map(this::toDto).toList();
    }

    public List<TransactionDto> getTransactions(User requester, TransactionFilterDto filter) {
        Specification<Transaction> spec;

        boolean isAdminOrModerator = requester.getRole() == Role.ADMIN
            || requester.getRole() == Role.MODERATOR;

        if (isAdminOrModerator && filter.getUserId() != null) {
            spec = TransactionSpecification.byUserId(filter.getUserId());
        } else if (isAdminOrModerator && Boolean.TRUE.equals(filter.getAllUsers())) {
            spec = (root, query, cb) -> null;
        } else {
            spec = TransactionSpecification.byUser(requester);
        }

        spec = spec.and(TransactionSpecification.byCategory(filter.getCategory()));
        spec = spec.and(TransactionSpecification.byType(filter.getType()));
        spec = spec.and(TransactionSpecification.byDateBetween(filter.getStartDate(), filter.getEndDate()));

        return transactionRepository.findAll(spec).stream().map(this::toDto).toList();
    }

    public TransactionDto updateTransaction(User user, UUID transaction_id, UpdateTransactionDto request) {
        Transaction transaction = transactionRepository.findById(transaction_id)
            .orElseThrow(() -> new NotFoundException("Transaction not found."));

        File file = transaction.getFile();
        boolean isOwner = file.getUser().getId().equals(user.getId());
        boolean isPrivileged = user.getRole() == Role.ADMIN || user.getRole() == Role.MODERATOR;
        if (!isOwner && !isPrivileged) {
            throw new UnauthorizedException("User is not authorized to make this action.");
        }

        transaction.setCategory(request.getCategory());
        transaction.setUpdatedAt(new Date());
        transactionRepository.save(transaction);

        return toDto(transaction);
    }

    public void deleteTransaction(User user, UUID transaction_id) {
        Transaction transaction = transactionRepository.findById(transaction_id)
            .orElseThrow(() -> new NotFoundException("Transaction not found."));

        File file = transaction.getFile();
        boolean isOwner = file.getUser().getId().equals(user.getId());
        boolean isPrivileged = user.getRole() == Role.ADMIN || user.getRole() == Role.MODERATOR;
        if (!isOwner && !isPrivileged) {
            throw new UnauthorizedException("User is not authorized to make this action.");
        }

        transaction.setActive(false);
        transaction.setDeletedAt(new Date());
        transactionRepository.save(transaction);
    }

    private TransactionDto toDto(Transaction t) {
        return new TransactionDto(
            t.getId(),
            t.getAmount(),
            t.getDate().toString(),
            t.getType().name(),
            t.getCategory().name(),
            t.getDescription()
        );
    }
}
