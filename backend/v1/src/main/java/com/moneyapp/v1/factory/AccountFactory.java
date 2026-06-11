package com.moneyapp.v1.factory;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.moneyapp.v1.enums.AccountType;
import com.moneyapp.v1.model.Account;
import com.moneyapp.v1.model.User;

@Component
public class AccountFactory {
    public Account create(String bankName, AccountType accountType, BigDecimal balance, String currency, User user) {
        Account account = new Account();
        account.setAccountType(accountType);
        account.setActive(true);
        account.setBalance(balance != null ? balance : BigDecimal.ZERO);
        account.setBankName(bankName);
        account.setCurrency(currency != null ? currency.toUpperCase() : "BRL");
        account.setUser(user);

        return account;
    }
}
