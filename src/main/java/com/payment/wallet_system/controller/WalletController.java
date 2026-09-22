package com.payment.wallet_system.controller;

import org.springframework.web.bind.annotation.RestController;

import com.payment.wallet_system.dto.WalletResponse;
import com.payment.wallet_system.service.WalletService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;



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
    
    
}
