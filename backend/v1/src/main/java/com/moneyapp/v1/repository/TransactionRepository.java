package com.moneyapp.v1.repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.moneyapp.v1.enums.TransactionType;
import com.moneyapp.v1.model.Transaction;
import com.moneyapp.v1.model.File;
import com.moneyapp.v1.model.User;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction>{
    List<Transaction> findByFileAndDate(File file, YearMonth referenceDate);
    List<Transaction> findByDate(YearMonth referenceDate);
    List<Transaction> findByFile(File file);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t " +
           "WHERE t.account.user = :user " +
           "AND t.type = :type " +
           "AND t.date >= :start AND t.date <= :end " +
           "AND t.active = true " +
           "GROUP BY t.category")
    List<Object[]> sumByCategoryForUser(
        @Param("user") User user,
        @Param("type") TransactionType type,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );
} 
