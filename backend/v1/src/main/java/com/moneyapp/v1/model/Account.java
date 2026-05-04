package com.moneyapp.v1.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import com.moneyapp.v1.enums.AccountType;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
    name = "accounts",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "bank_name", "account_type"})
)
@Getter
@Setter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(nullable = true)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Date createdAt = new Date();

    @Column(nullable = true)
    private Date updatedAt;

    @Column(nullable = true)
    private Date deletedAt;
}
