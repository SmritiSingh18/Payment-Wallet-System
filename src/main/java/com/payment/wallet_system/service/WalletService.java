package com.payment.wallet_system.service;


import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.payment.wallet_system.dto.AddMoneyRequest;
import com.payment.wallet_system.dto.TransferMoneyRequest;
import com.payment.wallet_system.dto.WalletResponse;
import com.payment.wallet_system.entity.Transaction;
import com.payment.wallet_system.entity.TransactionStatus;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.entity.Wallet;
import com.payment.wallet_system.entity.WalletStatus;
import com.payment.wallet_system.respository.TransactionRepository;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.respository.WalletRepository;

import jakarta.transaction.Transactional;

@Service 
public class WalletService {
    private  final WalletRepository walletRepository;
    private  final UserRepository userRepository;
    private  final TransactionRepository transactionRepository;
    public WalletService(WalletRepository walletRepository,UserRepository userRepository,TransactionRepository transactionRepository){
        this.walletRepository=walletRepository;
        this.userRepository=userRepository;
        this.transactionRepository=transactionRepository;
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
    @Transactional 
    public WalletResponse transferMoney(String email,TransferMoneyRequest request){
    
        User sender=userRepository
                        .findByEmail(email)
                        .orElseThrow(()-> new RuntimeException("Sender not found"));
        Wallet senderWallet=walletRepository
                            .findByUser(sender)
                            .orElseThrow(()-> new RuntimeException("Sender wallet not found"));

        User receiver=userRepository
                      .findByEmail(request.getReceiverEmail())
                      .orElseThrow(()-> new  RuntimeException("Receiver not found"));

        Wallet receiverWallet=walletRepository
                              .findByUser(receiver)
                              .orElseThrow(()->new  RuntimeException("Receiver Wallet not found"));

        if(sender.getId()==receiver.getId()){
            throw new RuntimeException("Cannot tranfer money to yourself");
        }
        if(senderWallet.getStatus()!=WalletStatus.ACTIVE ||
           receiverWallet.getStatus()!=WalletStatus.ACTIVE){
            throw new RuntimeException("Wallet is not active");
        }

        if(senderWallet.getBalance().compareTo(request.getAmount())<0){
            throw new RuntimeException("Insuffient balance");
        }

            senderWallet.setBalance(senderWallet.getBalance().subtract(request.getAmount()));
            receiverWallet.setBalance(receiverWallet.getBalance().add(request.getAmount()));

            walletRepository.save(senderWallet);
            Wallet saveReceiverWallet=walletRepository.save(receiverWallet);

            Transaction transaction=new Transaction();
            transaction.setTransactionId("TXN-"+UUID.randomUUID());
            transaction.setSenderWalletNumber(senderWallet.getWalletNumber());
            transaction.setReceiverWalletNumber(saveReceiverWallet.getWalletNumber());
            transaction.setAmount(request.getAmount());
            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setCreatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);

            

            WalletResponse response=new WalletResponse();
            response.setWalletNumber(saveReceiverWallet.getWalletNumber());
            response.setBalance(saveReceiverWallet.getBalance());
            response.setStatus(saveReceiverWallet.getStatus());

            return response;
        
    }
}
