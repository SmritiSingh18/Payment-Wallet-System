package com.payment.wallet_system.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.payment.wallet_system.entity.TransactionStatus;

import lombok.Data;

@Data 
public class TransactionResponse {
    private  String transactionId;
    private  String senderWalletNumber;
    private String receiverWalletNumber;
    private BigDecimal amount;
    private  TransactionStatus status;
    private  LocalDateTime createdAt;
}
