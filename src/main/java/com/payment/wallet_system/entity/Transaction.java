package com.payment.wallet_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Data 
@Table (name = "Transactions")
@Entity 
public class Transaction {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (unique = true,nullable = false)
    private String transactionId;

    @Column (nullable = false)
    private String senderWalletNumber;

    @Column (nullable = false)
    private String receiverWalletNumber;

    @Column (nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column (nullable = false)
    private TransactionStatus status;

    @Column (nullable = false)
    private LocalDateTime createdAt;

}
