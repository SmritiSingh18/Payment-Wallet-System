package com.payment.wallet_system.controller;

import org.springframework.web.bind.annotation.RestController;

import com.payment.wallet_system.dto.AddMoneyRequest;
import com.payment.wallet_system.dto.TransferMoneyRequest;
import com.payment.wallet_system.dto.WalletResponse;
import com.payment.wallet_system.service.WalletService;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;




@RestController
@RequestMapping ("/api/wallet")
public class WalletController {
    private  final WalletService walletService;
    public WalletController(WalletService walletService){
        this.walletService=walletService;
    }
    @GetMapping
    public WalletResponse getWallet() {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        String email=authentication.getName();
        return walletService.getWallet(email);
    }
    @PostMapping("/add-money")
    public WalletResponse addMoney(@RequestBody @Valid AddMoneyRequest request) {
         Authentication authentication=SecurityContextHolder.getContext()
                                       .getAuthentication();
        String email=authentication.getName();
        return  walletService.addMoney(email, request);
    }

    @PostMapping("/transfer")
    public WalletResponse transferMoney(@RequestBody @Valid TransferMoneyRequest request) {
       Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
       String email=authentication.getName();
        
        return walletService.transferMoney(email, request);
    }
    
    
    
    
}
