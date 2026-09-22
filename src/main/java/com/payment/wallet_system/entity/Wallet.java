package com.payment.wallet_system.entity;

import java.math.BigDecimal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data 
@Entity 
@Table (name = "wallets")
public class Wallet {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (unique = true)
    private String walletNumber;

    private  BigDecimal balance;
    
    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private  WalletStatus status;

    @OneToOne
    private User user;
    
}
