package com.payment.wallet_system.service;

import org.springframework.stereotype.Service;

import com.payment.wallet_system.dto.AddMoneyRequest;
import com.payment.wallet_system.dto.WalletResponse;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.entity.Wallet;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.respository.WalletRepository;

@Service 
public class WalletService {
    private  final WalletRepository walletRepository;
    private  final UserRepository userRepository;
    public WalletService(WalletRepository walletRepository,UserRepository userRepository){
        this.walletRepository=walletRepository;
        this.userRepository=userRepository;
    }
    public WalletResponse getWallet(String email){
        User user=userRepository
                  .findByEmail(email)
                  .orElseThrow(()-> new RuntimeException("User not found"));

        Wallet wallet=walletRepository
                      .findByUser(user)
                      .orElseThrow(()-> new RuntimeException("Wallet not found"));

        WalletResponse response=new WalletResponse();
        response.setWalletNumber(wallet.getWalletNumber());
        response.setBalance(wallet.getBalance());
        response.setStatus(wallet.getStatus());
        
        return response;
    }
    public  WalletResponse addMoney(String email,AddMoneyRequest request){
        User user=userRepository
                  .findByEmail(email)
                  .orElseThrow(() -> new RuntimeException("User not found"));

        Wallet wallet=walletRepository
                      .findByUser(user)
                      .orElseThrow(() -> new RuntimeException("Wallet not found"));

        wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        
        Wallet savedWallet=walletRepository.save(wallet);
        WalletResponse response=new WalletResponse();
        response.setWalletNumber(savedWallet.getWalletNumber());
        response.setBalance(savedWallet.getBalance());
        response.setStatus(savedWallet.getStatus());
        
        return  response;
    }
}
