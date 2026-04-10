package com.moneyapp.v1.repository;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.moneyapp.v1.model.Transaction;
import com.moneyapp.v1.model.File;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction>{
    List<Transaction> findByFileAndDate(File file, YearMonth referenceDate);
    List<Transaction> findByDate(YearMonth referenceDate);
    List<Transaction> findByFile(File file);
} 
