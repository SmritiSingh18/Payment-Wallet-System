package com.payment.wallet_system.service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.payment.wallet_system.dto.AddMoneyRequest;
import com.payment.wallet_system.dto.TransferMoneyRequest;
import com.payment.wallet_system.dto.WalletResponse;
import com.payment.wallet_system.entity.AuditEventType;
import com.payment.wallet_system.entity.IdempotencyRecord;
import com.payment.wallet_system.entity.IdempotencyStatus;
import com.payment.wallet_system.entity.Transaction;
import com.payment.wallet_system.entity.TransactionStatus;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.entity.Wallet;
import com.payment.wallet_system.entity.WalletStatus;
import com.payment.wallet_system.respository.IdempotencyRepository;
import com.payment.wallet_system.respository.TransactionRepository;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.respository.WalletRepository;

import jakarta.transaction.Transactional;

@Service 
public class WalletService {
    private final AuditLogService auditLogService;
    private  final WalletRepository walletRepository;
    private  final UserRepository userRepository;
    private  final TransactionRepository transactionRepository;
    private  final IdempotencyRepository idempotencyRepository;
  
    public WalletService(WalletRepository walletRepository,UserRepository userRepository,TransactionRepository transactionRepository,IdempotencyRepository idempotencyRepository, AuditLogService auditLogService){
        this.walletRepository=walletRepository;
        this.userRepository=userRepository;
        this.transactionRepository=transactionRepository;
        this.idempotencyRepository=idempotencyRepository;
        this.auditLogService = auditLogService;
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
         
        auditLogService.log(user.getId(),AuditEventType.MONEY_ADDED,"Money added:"+request.getAmount(),null);

        WalletResponse response=new WalletResponse();
        response.setWalletNumber(savedWallet.getWalletNumber());
        response.setBalance(savedWallet.getBalance());
        response.setStatus(savedWallet.getStatus());
        
        return  response;
    }

    
@Transactional
public WalletResponse transferMoney(
        String email,
        TransferMoneyRequest request,
        String idempotencyKey) {

    // 1. Validate the idempotency key
    if (idempotencyKey == null || idempotencyKey.isBlank()) {
        throw new IllegalArgumentException(
                "Idempotency key is required");
    }

    // 2. Check whether this request was already processed
    Optional<IdempotencyRecord> existingRecord =
            idempotencyRepository.findByIdempotencyKey(idempotencyKey);

    if (existingRecord.isPresent()) {
        IdempotencyRecord record = existingRecord.get();

        if (record.getStatus() == IdempotencyStatus.SUCCESS) {
            Transaction transaction = transactionRepository
                    .findByTransactionId(record.getTransactionId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Original transaction not found"));

            Wallet receiverWallet = walletRepository
                    .findByWalletNumber(
                            transaction.getReceiverWalletNumber())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Receiver wallet not found"));

            WalletResponse response = new WalletResponse();
            response.setWalletNumber(receiverWallet.getWalletNumber());
            response.setBalance(receiverWallet.getBalance());
            response.setStatus(receiverWallet.getStatus());

            return response;
        }

        throw new RuntimeException(
                "Idempotency key has already been used");
    }

    // 3. Find sender and receiver
    User sender = userRepository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("Sender not found"));

    User receiver = userRepository
            .findByEmail(request.getReceiverEmail())
            .orElseThrow(() ->
                    new RuntimeException("Receiver not found"));

    // 4. Prevent transferring to yourself
    if (sender.getId() == receiver.getId()) {
        throw new RuntimeException(
                "Cannot transfer money to yourself");
    }

    // 5. Lock wallets in a consistent order
    // Both opposite-direction transfers will lock the
    // lower user ID's wallet first.
    Wallet senderWallet;
    Wallet receiverWallet;

    if (sender.getId() < receiver.getId()) {

        senderWallet = walletRepository
                .findWithLockByUser(sender)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Sender wallet not found"));

        receiverWallet = walletRepository
                .findWithLockByUser(receiver)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Receiver wallet not found"));

    } else {

        receiverWallet = walletRepository
                .findWithLockByUser(receiver)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Receiver wallet not found"));

        senderWallet = walletRepository
                .findWithLockByUser(sender)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Sender wallet not found"));
    }

    // 6. Validate wallet status
    if (senderWallet.getStatus() != WalletStatus.ACTIVE
            || receiverWallet.getStatus() != WalletStatus.ACTIVE) {
        throw new RuntimeException("Wallet is not active");
    }

    // 7. Validate the amount
    if (request.getAmount() == null
            || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException(
                "Transfer amount must be greater than zero");
    }

    // 8. Check sufficient balance
    if (senderWallet.getBalance()
            .compareTo(request.getAmount()) < 0) {

        auditLogService.log(
                sender.getId(),
                AuditEventType.TRANSFER_FAILED,
                "Transfer failed - insufficient balance",
                null);

        throw new RuntimeException("Insufficient balance");
    }

    // 9. Update balances
    senderWallet.setBalance(
            senderWallet.getBalance().subtract(request.getAmount()));

    receiverWallet.setBalance(
            receiverWallet.getBalance().add(request.getAmount()));

    walletRepository.save(senderWallet);
    Wallet savedReceiverWallet =
            walletRepository.save(receiverWallet);

    // 10. Create transaction record
    Transaction transaction = new Transaction();
    transaction.setTransactionId(
            "TXN-" + UUID.randomUUID());
    transaction.setSenderWalletNumber(
            senderWallet.getWalletNumber());
    transaction.setReceiverWalletNumber(
            savedReceiverWallet.getWalletNumber());
    transaction.setAmount(request.getAmount());
    transaction.setStatus(TransactionStatus.SUCCESS);
    transaction.setCreatedAt(LocalDateTime.now());

    Transaction savedTransaction =
            transactionRepository.save(transaction);

    // 11. Save idempotency record
    IdempotencyRecord idempotencyRecord =
            new IdempotencyRecord();

    idempotencyRecord.setIdempotencyKey(idempotencyKey);
    idempotencyRecord.setTransactionId(
            savedTransaction.getTransactionId());
    idempotencyRecord.setUserId(sender.getId());
    idempotencyRecord.setStatus(IdempotencyStatus.SUCCESS);
    idempotencyRecord.setCreatedAt(LocalDateTime.now());

    idempotencyRepository.save(idempotencyRecord);

    // 12. Record successful transfer
    auditLogService.logFailure(
            sender.getId(),
            AuditEventType.TRANSFER_SUCCESS,
            "Transfer success",
            savedTransaction.getId());

    // 13. Return receiver wallet details
    WalletResponse response = new WalletResponse();
    response.setWalletNumber(savedReceiverWallet.getWalletNumber());
    response.setBalance(savedReceiverWallet.getBalance());
    response.setStatus(savedReceiverWallet.getStatus());

    return response;
}
}