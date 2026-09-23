package com.payment.wallet_system.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data 
public class AddMoneyRequest {
    @NotNull 
    @DecimalMin (value = "1.00")
    private  BigDecimal amount;
    
}
