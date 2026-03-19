package com.moneyapp.v1.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.moneyapp.v1.model.Expense;
import com.moneyapp.v1.model.File;
import com.moneyapp.v1.model.User;
import com.moneyapp.v1.repository.ExpenseRepository;
import com.moneyapp.v1.dto.ExpenseResponseDTO;
import com.moneyapp.v1.enums.ExpenseCategory;
import com.moneyapp.v1.enums.TransactionType;
import com.moneyapp.v1.exception.InvalidRequestException;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Expense> saveExpenses(String json, File file, User user) throws Exception {
        String cleanJson = json
            .replaceAll("```json", "")
            .replaceAll("```", "")
            .trim();

        ExpenseResponseDTO response;
        try {
            response = objectMapper.readValue(cleanJson, ExpenseResponseDTO.class);
        } catch (JsonProcessingException e) {
            throw new InvalidRequestException("Error parsing AI response: " + e.getMessage());
        }

        List<Expense> expenses = response.transactions().stream().map(item -> {
            Expense expense = new Expense();
            expense.setAmount(item.amount());
            expense.setDate(LocalDate.parse(item.date(), DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            expense.setType(TransactionType.valueOf(item.type()));
            expense.setCategory(ExpenseCategory.valueOf(item.category()));
            expense.setDescription(item.description());
            expense.setUser(user);
            expense.setFile(file);
            return expense;
        }).toList();

        return expenseRepository.saveAll(expenses);
    }

    public List<Expense> getExpensesByUser(User user) {
        return expenseRepository.findByUser(user);
    }

    public List<Expense> getExpensesByFileAndUser(UUID fileId, User user) {
        return expenseRepository.findByFileIdAndUser(fileId, user);
    }
}