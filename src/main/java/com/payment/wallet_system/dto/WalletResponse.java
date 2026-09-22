package com.payment.wallet_system.dto;

import java.math.BigDecimal;

import com.payment.wallet_system.entity.WalletStatus;

import lombok.Data;

@Data 
public class WalletResponse {
    private  String walletNumber;
    private  BigDecimal balance;
    private WalletStatus status;
    
}