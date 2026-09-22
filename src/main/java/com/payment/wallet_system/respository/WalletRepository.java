package com.payment.wallet_system.respository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.entity.Wallet;



public interface WalletRepository extends JpaRepository<Wallet,Long> {
    Optional<Wallet> findByUser(User user);
}
