package com.moneyapp.v1.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moneyapp.v1.enums.AccountType;
import com.moneyapp.v1.model.Account;
import com.moneyapp.v1.model.User;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByUserAndActiveTrue(User user);

    Optional<Account> findByUserAndBankNameAndAccountType(User user, String bankName, AccountType accountType);

    Optional<Account> findByIdAndUser(UUID id, User user);
}
