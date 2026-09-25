package com.payment.wallet_system.respository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payment.wallet_system.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
    List<Transaction> findBySenderWalletNumberOrReceiverWalletNumber(String senderWalletNumber,String receiverWalletNumber);
    Optional<Transaction> findByTransactionId(String transacationId);
    
}
