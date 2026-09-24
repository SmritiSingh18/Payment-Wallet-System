package com.payment.wallet_system.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payment.wallet_system.dto.TransactionResponse;
import com.payment.wallet_system.service.TransactionService;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;


@RestController 
@RequestMapping ("/api/transactions")
public class TransactionController {
    private  final TransactionService transactionService;
    public TransactionController(TransactionService transactionService){
        this.transactionService=transactionService;
    }
    @GetMapping
    public List<TransactionResponse> getTansactionHistory() {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        return transactionService.getTransactionDetails(email);

    }
    
    
}
