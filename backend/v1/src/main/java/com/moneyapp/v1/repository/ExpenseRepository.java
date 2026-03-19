package com.moneyapp.v1.repository;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moneyapp.v1.model.Expense;
import com.moneyapp.v1.model.File;
import com.moneyapp.v1.model.User;

public interface ExpenseRepository extends JpaRepository<Expense, UUID>{
    List<Expense> findByUser(User user);
    List<Expense> findByUserAndDate(User user, YearMonth referenceDate);
    List<Expense> findByFile(File file);
    List<Expense> findByFileIdAndUser(UUID fileId, User user);
} 
