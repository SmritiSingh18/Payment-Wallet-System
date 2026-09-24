package com.payment.wallet_system.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.payment.wallet_system.dto.TransactionResponse;
import com.payment.wallet_system.entity.Transaction;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.entity.Wallet;
import com.payment.wallet_system.respository.TransactionRepository;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.respository.WalletRepository;

@Service 
public class TransactionService {
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public  TransactionService(UserRepository userRepository,WalletRepository walletRepository,TransactionRepository transactionRepository){
        this.userRepository=userRepository;
        this.walletRepository=walletRepository;
        this.transactionRepository=transactionRepository;
    }

    public List<TransactionResponse> getTransactionDetails(String email){
        User user=userRepository
                  .findByEmail(email)
                  .orElseThrow(()-> new  RuntimeException("User not  found"));

        Wallet wallet=walletRepository
                      .findByUser(user)
                      .orElseThrow(()-> new RuntimeException("Wallet not found"));

        String walletNumber=wallet.getWalletNumber();
        List<Transaction> transactions=transactionRepository
                                    .findBySenderWalletNumberOrReceiverWalletNumber(walletNumber, walletNumber);

        return transactions.stream()
            .map(transaction ->{
                TransactionResponse response=new TransactionResponse();
                response.setTransactionId(transaction.getTransactionId());
                response.setSenderWalletNumber(transaction.getSenderWalletNumber());
                response.setReceiverWalletNumber(transaction.getReceiverWalletNumber());
                response.setAmount(transaction.getAmount());
                response.setStatus(transaction.getStatus());
                response.setCreatedAt(transaction.getCreatedAt());

                return  response;

            }).toList();
    }
}
