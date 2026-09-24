package com.payment.wallet_system.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data 
public class TransferMoneyRequest {
    @NotBlank 
    @Email 
    private String receiverEmail;
    
    @NotNull 
    @DecimalMin (value = "1.00")
    private  BigDecimal amount;
    
}
